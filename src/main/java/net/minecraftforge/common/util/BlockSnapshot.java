package net.minecraftforge.common.util;

import net.minecraft.Block;
import net.minecraft.ItemStack;
import net.minecraft.World;

import java.util.ArrayList;
import java.util.List;

public class BlockSnapshot {
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final Block block;
    private final int metadata;

    public BlockSnapshot(World world, int x, int y, int z, Block block, int metadata) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.metadata = metadata;
    }

    public World getWorld() {
        return this.world;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public Block getBlock() {
        return this.block;
    }

    public int getMetadata() {
        return this.metadata;
    }
}
