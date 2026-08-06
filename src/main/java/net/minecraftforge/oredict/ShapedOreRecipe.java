package net.minecraftforge.oredict;

import net.minecraft.Block;
import net.minecraft.Item;
import net.minecraft.ItemStack;

public class ShapedOreRecipe {
    public final ItemStack output;
    public final Object[] recipe;

    public ShapedOreRecipe(Block result, Object... recipe) {
        this(new ItemStack(result), recipe);
    }

    public ShapedOreRecipe(Item result, Object... recipe) {
        this(new ItemStack(result), recipe);
    }

    public ShapedOreRecipe(ItemStack result, Object... recipe) {
        this.output = result;
        this.recipe = recipe;
    }

    public ItemStack getRecipeOutput() {
        return this.output.copy();
    }

    public Object[] getInput() {
        return this.recipe;
    }
}
