package codechicken.nei.recipe;

import codechicken.nei.PositionedStack;
import net.minecraft.ItemStack;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Minimal NEI recipe-handler shim so the optional integration compiles.
 */
public class TemplateRecipeHandler implements IRecipeHandler {
    protected final List<CachedRecipe> arecipes = new ArrayList<>();
    protected final List<RecipeTransferRect> transferRects = new ArrayList<>();
    public int cycleticks;

    public String getRecipeName() {
        return "ProjectE";
    }

    public String getGuiTexture() {
        return "projecte:textures/gui/nei.png";
    }

    public void loadCraftingRecipes(String outputId, Object... results) {
    }

    public void loadCraftingRecipes(ItemStack result) {
    }

    public void loadUsageRecipes(ItemStack ingredient) {
    }

    public void loadTransferRects() {
    }

    public void drawForeground(int recipe) {
    }

    public boolean isRecipe2x2(int recipe) {
        return false;
    }

    @Override
    public int numRecipes() {
        return this.arecipes.size();
    }

    public List<PositionedStack> getIngredientStacks(int recipe) {
        CachedRecipe cachedRecipe = this.arecipes.get(recipe);
        return cachedRecipe == null ? new ArrayList<>() : cachedRecipe.getIngredients();
    }

    public PositionedStack getResultStack(int recipe) {
        CachedRecipe cachedRecipe = this.arecipes.get(recipe);
        return cachedRecipe == null ? null : cachedRecipe.getResult();
    }

    public List<PositionedStack> getCycledIngredients(int cycle, List<PositionedStack> ingredients) {
        for (int i = 0; i < ingredients.size(); i++) {
            ingredients.get(i).setPermutationToRender(cycle);
        }
        return ingredients;
    }

    public static class CachedRecipe {
        public List<PositionedStack> getIngredients() {
            return new ArrayList<>();
        }

        public PositionedStack getResult() {
            return null;
        }

        public PositionedStack getIngredient() {
            return null;
        }

        public PositionedStack getOtherStack() {
            return null;
        }
    }

    public static class RecipeTransferRect {
        public final Rectangle rect;
        public final String outputId;
        public final Object[] results;

        public RecipeTransferRect(Rectangle rect, String outputId, Object... results) {
            this.rect = rect;
            this.outputId = outputId;
            this.results = results;
        }
    }
}
