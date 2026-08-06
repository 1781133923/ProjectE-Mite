package net.minecraftforge.client;

import net.minecraft.ItemStack;
import org.lwjgl.opengl.GL11;

public interface IItemRenderer {
    boolean handleRenderType(ItemStack item, ItemRenderType type);

    boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper);

    void renderItem(ItemRenderType type, ItemStack item, Object... data);

    enum ItemRenderType {
        ENTITY,
        EQUIPPED,
        EQUIPPED_FIRST_PERSON,
        INVENTORY,
        ENTITY_ROTATING
    }

    enum ItemRendererHelper {
        ENTITY_ROTATION,
        ENTITY_BOBBING,
        EQUIPPED_BLOCK,
        BLOCK_3D,
        INVENTORY_BLOCK
    }
}
