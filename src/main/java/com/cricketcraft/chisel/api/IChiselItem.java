package com.cricketcraft.chisel.api;

import net.minecraft.ItemStack;

public interface IChiselItem {
    boolean canChisel(net.minecraft.World world, ItemStack chisel, ItemStack target);
}
