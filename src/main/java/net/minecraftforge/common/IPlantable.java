package net.minecraftforge.common;

import net.minecraft.Block;
import net.minecraft.World;

public interface IPlantable {
    Object getPlant(World world, int x, int y, int z);

    Object getPlantType(World world, int x, int y, int z);

    boolean canPlaceBlockOn(Block block);

    default int getPlantMetadata(net.minecraft.World world, int x, int y, int z) {
        return 0;
    }
}
