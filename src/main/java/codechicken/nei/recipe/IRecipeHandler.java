package codechicken.nei.recipe;

import net.minecraft.ItemStack;

import java.util.List;

public interface IRecipeHandler {
    String getRecipeName();

    void loadCraftingRecipes(String outputId, Object... results);

    void loadCraftingRecipes(ItemStack result);

    void loadUsageRecipes(ItemStack ingredient);

    int numRecipes();

    List<?> getIngredientStacks(int recipe);

    Object getResultStack(int recipe);
}
