package net.minecraftforge.common.util;

import net.minecraft.EntityPlayer;
import net.minecraft.World;

public class FakePlayer extends EntityPlayer {
    public FakePlayer(World world) {
        super(world, "[ProjectE]");
    }

    @Override
    public net.minecraft.INetworkManager getNetManager() {
        return null;
    }

    @Override
    public net.minecraft.ChunkCoordinates getPlayerCoordinates() {
        return new net.minecraft.ChunkCoordinates((int) this.posX, (int) this.posY, (int) this.posZ);
    }

    @Override
    public boolean canCommandSenderUseCommand(int permissionLevel, String command) {
        return false;
    }

    @Override
    public void sendChatToPlayer(net.minecraft.ChatMessageComponent message) {
    }
}
