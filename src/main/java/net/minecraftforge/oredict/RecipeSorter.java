package net.minecraftforge.oredict;

import net.minecraft.IRecipe;

import java.util.HashMap;
import java.util.Map;

public class RecipeSorter {
    private static final Map<String, String> REGISTERED = new HashMap<>();

    public static void register(String name, Class<? extends IRecipe> recipe, Category category, String... dependencies) {
        REGISTERED.put(name, recipe.getName());
    }

    public enum Category {
        SHAPED,
        SHAPELESS
    }
}
