package com.github.Debris.HelloFML.blocks;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.Material;
import net.xiaoyu233.fml.reload.event.ItemRegistryEvent;
import net.xiaoyu233.fml.reload.event.RecipeRegistryEvent;

public class Blocks extends Block {
    protected Blocks(int par1, Material par2Material, BlockConstants constants) {
        super(par1, par2Material, constants);
    }

    //在这里注册影子物品
    public static void registerItemBlocks(ItemRegistryEvent registryEvent) {

    }

    //在这里注册影子物品合成配方
    public static void registerRecipes(RecipeRegistryEvent register) {

    }
}
