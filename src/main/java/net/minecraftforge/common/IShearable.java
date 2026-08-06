package net.minecraftforge.common;

import net.minecraft.ItemStack;
import net.minecraft.World;

import java.util.ArrayList;

public interface IShearable {
    boolean isShearable(ItemStack item, World world, int x, int y, int z);

    ArrayList<ItemStack> onSheared(ItemStack item, World world, int x, int y, int z, int fortune);
}
