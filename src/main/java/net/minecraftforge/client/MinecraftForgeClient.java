package net.minecraftforge.client;

import net.minecraft.Item;

import java.util.HashMap;
import java.util.Map;

public class MinecraftForgeClient {
    private static final Map<Item, IItemRenderer> ITEM_RENDERERS = new HashMap<>();

    public static void registerItemRenderer(Item item, IItemRenderer renderer) {
        ITEM_RENDERERS.put(item, renderer);
    }

    public static IItemRenderer getItemRenderer(Item item) {
        return ITEM_RENDERERS.get(item);
    }
}
