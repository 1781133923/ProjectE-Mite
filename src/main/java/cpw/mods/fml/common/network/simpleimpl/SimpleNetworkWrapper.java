package cpw.mods.fml.common.network.simpleimpl;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import moddedmite.rustedironcore.network.Network;
import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import moddedmite.rustedironcore.network.PacketReader;
import net.minecraft.EntityPlayer;
import net.minecraft.Minecraft;
import net.minecraft.Packet250CustomPayload;
import net.minecraft.ResourceLocation;
import net.minecraft.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * SimpleNetworkWrapper replacement built on MITE's Packet250CustomPayload and
 * RustedIronCore's packet reader/dispatch infrastructure.
 */
public class SimpleNetworkWrapper {
    private final ResourceLocation channel;

    private final Map<Integer, Class<? extends IMessage>> messageClasses = new HashMap<>();
    private final Map<Integer, Class<? extends IMessageHandler>> handlerClasses = new HashMap<>();
    private final Map<Integer, Side> sides = new HashMap<>();

    public SimpleNetworkWrapper(String channelName) {
        this.channel = new ResourceLocation(channelName);
        PacketReader.registerServerPacketReader(this.channel, buf -> SimpleNetworkWrapper.this.read(buf, true));
        PacketReader.registerClientPacketReader(this.channel, buf -> SimpleNetworkWrapper.this.read(buf, false));
    }

    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
            Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, int discriminator, Side side) {
        this.messageClasses.put(discriminator, requestMessageType);
        this.handlerClasses.put(discriminator, messageHandler);
        this.sides.put(discriminator, side);
    }

    public Packet250CustomPayload getPacketFrom(IMessage message) {
        io.netty.buffer.ByteBuf ourBuf = new io.netty.buffer.ByteBuf();
        ourBuf.writeByte(findDiscriminator(message));
        message.toBytes(ourBuf);
        return new Packet250CustomPayload(this.channel.toString(), ourBuf.array());
    }

    private Packet read(PacketByteBuf buf, boolean serverSide) {
        int discriminator = buf.readByte() & 0xFF;
        Class<? extends IMessage> messageClass = this.messageClasses.get(discriminator);
        Class<? extends IMessageHandler> handlerClass = this.handlerClasses.get(discriminator);
        if (messageClass == null || handlerClass == null) {
            throw new IllegalStateException("Unknown ProjectE packet discriminator " + discriminator);
        }
        try {
            IMessage message = messageClass.getDeclaredConstructor().newInstance();
            io.netty.buffer.ByteBuf ourBuf = new io.netty.buffer.ByteBuf();
            ourBuf.setDelegate(buf);
            message.fromBytes(ourBuf);
            return new DispatchPacket(discriminator, message, handlerClass, serverSide);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to construct packet " + messageClass, e);
        }
    }

    public void sendToServer(IMessage message) {
        Network.sendToServer(wrap(message));
    }

    public void sendToAll(IMessage message) {
        Packet250CustomPayload vanilla = getPacketFrom(message);
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return;
        }
        for (Object player : server.getConfigurationManager().playerEntityList) {
            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).playerNetServerHandler.sendPacketToPlayer(vanilla);
            }
        }
    }

    public void sendTo(IMessage message, ServerPlayer player) {
        if (player == null) {
            return;
        }
        player.playerNetServerHandler.sendPacketToPlayer(getPacketFrom(message));
    }

    public void sendToAllAround(IMessage message, TargetPoint point) {
        Packet250CustomPayload vanilla = getPacketFrom(message);
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return;
        }
        for (Object player : server.getConfigurationManager().playerEntityList) {
            if (player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer) player;
                if (sp.dimension == point.getDimension()) {
                    double dx = sp.posX - point.getX();
                    double dy = sp.posY - point.getY();
                    double dz = sp.posZ - point.getZ();
                    if (dx * dx + dy * dy + dz * dz < point.getRange() * point.getRange()) {
                        sp.playerNetServerHandler.sendPacketToPlayer(vanilla);
                    }
                }
            }
        }
    }

    public void sendToDimension(IMessage message, int dimensionId) {
        Packet250CustomPayload vanilla = getPacketFrom(message);
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return;
        }
        for (Object player : server.getConfigurationManager().playerEntityList) {
            if (player instanceof ServerPlayer && ((ServerPlayer) player).dimension == dimensionId) {
                ((ServerPlayer) player).playerNetServerHandler.sendPacketToPlayer(vanilla);
            }
        }
    }

    private Packet wrap(IMessage message) {
        io.netty.buffer.ByteBuf ourBuf = new io.netty.buffer.ByteBuf();
        ourBuf.writeByte(findDiscriminator(message));
        message.toBytes(ourBuf);
        return new SimplePacket(this.channel, ourBuf.array());
    }

    private int findDiscriminator(IMessage message) {
        for (Map.Entry<Integer, Class<? extends IMessage>> entry : this.messageClasses.entrySet()) {
            if (entry.getValue().isInstance(message)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Message not registered: " + message.getClass());
    }

    private static class SimplePacket implements Packet {
        private final byte[] data;
        private final ResourceLocation channel;

        private SimplePacket(ResourceLocation channel, byte[] data) {
            this.data = data;
            this.channel = channel;
        }

        @Override
        public void write(PacketByteBuf packetByteBuf) {
            for (byte b : this.data) {
                packetByteBuf.writeByte(b);
            }
        }

        @Override
        public void apply(EntityPlayer entityPlayer) {
        }

        @Override
        public ResourceLocation getChannel() {
            return this.channel;
        }
    }

    private class DispatchPacket implements Packet {
        private final int discriminator;
        private final IMessage message;
        private final Class<? extends IMessageHandler> handlerClass;
        private final boolean serverSide;

        private DispatchPacket(int discriminator, IMessage message, Class<? extends IMessageHandler> handlerClass, boolean serverSide) {
            this.discriminator = discriminator;
            this.message = message;
            this.handlerClass = handlerClass;
            this.serverSide = serverSide;
        }

        @Override
        public void write(PacketByteBuf packetByteBuf) {
            // Received packets are never re-serialized.
        }

        @Override
        public void apply(EntityPlayer player) {
            try {
                IMessageHandler handler = this.handlerClass.getDeclaredConstructor().newInstance();
                if (this.serverSide) {
                    handler.onMessage(this.message, new MessageContext(Side.SERVER, ((ServerPlayer) player).playerNetServerHandler, null));
                } else {
                    handler.onMessage(this.message, new MessageContext(Side.CLIENT, null, Minecraft.getMinecraft().getNetHandler()));
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException("Failed to invoke packet handler " + this.handlerClass, e);
            }
        }

        @Override
        public ResourceLocation getChannel() {
            return SimpleNetworkWrapper.this.channel;
        }
    }
}
