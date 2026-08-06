package net.minecraftforge.event.world;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.World;

public class BlockEvent extends Event {
    public final int x;
    public final int y;
    public final int z;
    public final World world;
    public final Block block;

    public BlockEvent(int x, int y, int z, World world, Block block) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.block = block;
    }

    public static class BreakEvent extends BlockEvent {
        public final EntityPlayer player;
        public int exp;

        public BreakEvent(int x, int y, int z, World world, Block block, EntityPlayer player) {
            super(x, y, z, world, block);
            this.player = player;
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    public static class PlaceEvent extends BlockEvent {
        public final Block placedAgainst;
        public final EntityPlayer player;

        public PlaceEvent(Block placedAgainst, Block newBlock, EntityPlayer player) {
            super(player == null ? 0 : (int) player.posX, player == null ? 0 : (int) player.posY,
                    player == null ? 0 : (int) player.posZ, player == null ? null : player.worldObj, newBlock);
            this.placedAgainst = placedAgainst;
            this.player = player;
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }
}
