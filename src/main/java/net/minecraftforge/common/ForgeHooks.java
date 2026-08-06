package net.minecraftforge.common;

import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.World;

public class ForgeHooks {
    public static boolean canToolHarvestBlock(Block block, net.minecraft.ItemStack stack) {
        return stack != null;
    }

    public static boolean canToolHarvestBlock(Block block, EntityPlayer player, int metadata) {
        return true;
    }

    public static boolean canToolHarvestBlock(Block block, int metadata, net.minecraft.ItemStack stack) {
        return true;
    }

    public static int onBlockBreakEvent(World world, int gameType, EntityPlayer player, int x, int y, int z) {
        return 0;
    }
}
