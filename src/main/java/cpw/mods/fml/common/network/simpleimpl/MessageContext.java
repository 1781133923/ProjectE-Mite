package cpw.mods.fml.common.network.simpleimpl;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.NetClientHandler;
import net.minecraft.NetServerHandler;

public class MessageContext {
    public final Side side;
    private final NetServerHandler serverHandler;
    private final NetClientHandler clientHandler;

    public MessageContext(Side side, NetServerHandler serverHandler, NetClientHandler clientHandler) {
        this.side = side;
        this.serverHandler = serverHandler;
        this.clientHandler = clientHandler;
    }

    public NetServerHandler getServerHandler() {
        return this.serverHandler;
    }

    public NetClientHandler getClientHandler() {
        return this.clientHandler;
    }
}
