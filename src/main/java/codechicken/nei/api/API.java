package codechicken.nei.api;

import codechicken.nei.recipe.IRecipeHandler;

import java.util.ArrayList;
import java.util.List;

public class API {
    private static final List<IRecipeHandler> RECIPE_HANDLERS = new ArrayList<>();
    private static final List<IRecipeHandler> USAGE_HANDLERS = new ArrayList<>();

    public static void registerRecipeHandler(IRecipeHandler handler) {
        RECIPE_HANDLERS.add(handler);
    }

    public static void registerUsageHandler(IRecipeHandler handler) {
        USAGE_HANDLERS.add(handler);
    }
}
