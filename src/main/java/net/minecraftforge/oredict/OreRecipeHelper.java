package net.minecraftforge.oredict;

import net.minecraft.Block;
import net.minecraft.Item;
import net.minecraft.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Expands ore-dictionary recipes into concrete ItemStack recipes.
 */
public final class OreRecipeHelper {
    private OreRecipeHelper() {
    }

    public static List<Object[]> expand(ItemStack output, Object[] recipe) {
        List<Object[]> results = new ArrayList<>();
        expandRecursive(output, recipe, 0, new Object[recipe.length], results);
        return results;
    }

    public static List<Object[]> expandShapeless(ItemStack output, Object[] recipe) {
        List<Object[]> results = new ArrayList<>();
        expandRecursive(output, recipe, 0, new Object[recipe.length], results);
        return results;
    }

    private static void expandRecursive(ItemStack output, Object[] recipe, int index, Object[] current, List<Object[]> results) {
        if (index >= recipe.length) {
            Object[] copy = new Object[recipe.length + 1];
            copy[0] = output;
            System.arraycopy(current, 0, copy, 1, recipe.length);
            results.add(copy);
            return;
        }
        Object ingredient = recipe[index];
        if (ingredient instanceof String) {
            List<ItemStack> ores = OreDictionary.getOres((String) ingredient);
            if (ores.isEmpty()) {
                return;
            }
            for (ItemStack ore : ores) {
                current[index] = ore;
                expandRecursive(output, recipe, index + 1, current, results);
                if (results.size() > 64) {
                    return;
                }
            }
        } else if (ingredient instanceof ItemStack) {
            current[index] = ingredient;
            expandRecursive(output, recipe, index + 1, current, results);
        } else if (ingredient instanceof Item) {
            current[index] = new ItemStack((Item) ingredient);
            expandRecursive(output, recipe, index + 1, current, results);
        } else if (ingredient instanceof Block) {
            current[index] = new ItemStack((Block) ingredient);
            expandRecursive(output, recipe, index + 1, current, results);
        } else if (ingredient == null) {
            current[index] = null;
            expandRecursive(output, recipe, index + 1, current, results);
        }
    }
}
