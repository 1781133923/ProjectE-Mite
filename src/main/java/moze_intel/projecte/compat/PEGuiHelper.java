package moze_intel.projecte.compat;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.Container;
import net.minecraft.EntityPlayer;
import net.minecraft.GuiContainer;
import net.minecraft.GuiScreen;
import net.minecraft.ICrafting;
import net.minecraft.Minecraft;
import net.minecraft.ServerPlayer;
import net.minecraft.World;
import net.xiaoyu233.fml.reload.utils.IdUtil;

/**
 * Replaces Forge's EntityPlayer.openGui for the MITE port.
 */
public final class PEGuiHelper {
    private static final SimpleNetworkWrapper GUI_NETWORK = new SimpleNetworkWrapper("projecte:gui");

    static {
        GUI_NETWORK.registerMessage(OpenGuiPKT.Handler.class, OpenGuiPKT.class, 0, Side.CLIENT);
        GUI_NETWORK.registerMessage(OpenGuiRequestPKT.Handler.class, OpenGuiRequestPKT.class, 1, Side.SERVER);
    }

    private static int nextWindowId = 1;

    private PEGuiHelper() {
    }

    public static void openGui(EntityPlayer player, int guiId, World world, int x, int y, int z) {
        if (world.isRemote) {
            GUI_NETWORK.sendToServer(new OpenGuiRequestPKT(guiId, x, y, z));
            return;
        }
        IGuiHandler handler = NetworkRegistry.INSTANCE.getGuiHandler();
        if (handler == null) {
            return;
        }
        Object serverElement = handler.getServerGuiElement(guiId, player, world, x, y, z);
        if (!(serverElement instanceof Container)) {
            return;
        }
        Container container = (Container) serverElement;
        int windowId = nextWindowId++;
        player.openContainer = container;
        container.windowId = windowId;
        if (player instanceof ServerPlayer) {
            // Tell the client to open the GUI *first*: Packet104WindowItems
            // (sent by addCraftingToCrafters below) is dropped by the client
            // unless it is processed after the GUI is open, so any slots
            // filled while the world was saved would otherwise appear empty
            // until the player clicks a slot (which triggers a single-slot
            // Packet103SetSlot refresh).
            GUI_NETWORK.sendTo(new OpenGuiPKT(guiId, windowId, x, y, z), (ServerPlayer) player);
            // Register the player as a listener so the server pushes the
            // container's slot contents/progress to the client.
            container.addCraftingToCrafters((ICrafting) player);
        }
    }

    public static class OpenGuiPKT implements IMessage {
        private int guiId;
        private int windowId;
        private int x;
        private int y;
        private int z;

        public OpenGuiPKT() {
        }

        public OpenGuiPKT(int guiId, int windowId, int x, int y, int z) {
            this.guiId = guiId;
            this.windowId = windowId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.guiId = buf.readInt();
            this.windowId = buf.readInt();
            this.x = buf.readInt();
            this.y = buf.readInt();
            this.z = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(this.guiId);
            buf.writeInt(this.windowId);
            buf.writeInt(this.x);
            buf.writeInt(this.y);
            buf.writeInt(this.z);
        }

        public static class Handler implements IMessageHandler<OpenGuiPKT, IMessage> {
            @Override
            public IMessage onMessage(OpenGuiPKT message, MessageContext ctx) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.thePlayer == null || mc.theWorld == null) {
                    return null;
                }
                IGuiHandler handler = NetworkRegistry.INSTANCE.getGuiHandler();
                if (handler == null) {
                    return null;
                }
                Object clientElement = handler.getClientGuiElement(message.guiId, mc.thePlayer, mc.theWorld, message.x, message.y, message.z);
                if (clientElement instanceof GuiContainer) {
                    // The GUI builds its own internal container; match it to the
                    // server-side window id so slot clicks and close packets target
                    // the correct container.
                    GuiContainer gui = (GuiContainer) clientElement;
                    gui.inventorySlots.windowId = message.windowId;
                    mc.thePlayer.openContainer = gui.inventorySlots;
                } else if (clientElement instanceof GuiScreen) {
                    Object serverElement = handler.getServerGuiElement(message.guiId, mc.thePlayer, mc.theWorld, message.x, message.y, message.z);
                    if (serverElement instanceof Container) {
                        Container container = (Container) serverElement;
                        container.windowId = message.windowId;
                        mc.thePlayer.openContainer = container;
                    }
                }
                if (clientElement instanceof GuiScreen) {
                    mc.displayGuiScreen((GuiScreen) clientElement);
                }
                return null;
            }
        }
    }

    public static class OpenGuiRequestPKT implements IMessage {
        private int guiId;
        private int x;
        private int y;
        private int z;

        public OpenGuiRequestPKT() {
        }

        public OpenGuiRequestPKT(int guiId, int x, int y, int z) {
            this.guiId = guiId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.guiId = buf.readInt();
            this.x = buf.readInt();
            this.y = buf.readInt();
            this.z = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(this.guiId);
            buf.writeInt(this.x);
            buf.writeInt(this.y);
            buf.writeInt(this.z);
        }

        public static class Handler implements IMessageHandler<OpenGuiRequestPKT, IMessage> {
            @Override
            public IMessage onMessage(OpenGuiRequestPKT message, MessageContext ctx) {
                if (ctx.getServerHandler() != null && ctx.getServerHandler().playerEntity != null) {
                    ServerPlayer player = ctx.getServerHandler().playerEntity;
                    openGui(player, message.guiId, player.worldObj, message.x, message.y, message.z);
                }
                return null;
            }
        }
    }
}
