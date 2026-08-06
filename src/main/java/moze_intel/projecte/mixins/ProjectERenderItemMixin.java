package moze_intel.projecte.mixins;

import net.minecraft.FontRenderer;
import net.minecraft.ItemStack;
import net.minecraft.RenderItem;
import net.minecraft.TextureManager;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE's RenderItem already renders 3D blocks in the inventory GUI with its
 * own transforms (RenderBlocks.renderBlockAsItem). Replace just that call with
 * ProjectE's model renderer, so the chest/condenser/pedestal appear as their
 * actual model at the correct slot position.
 */
@Mixin(RenderItem.class)
public abstract class ProjectERenderItemMixin {
    @Inject(method = "renderItemIntoGUI",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/RenderBlocks;renderBlockAsItem(Lnet/minecraft/Block;IF)V"),
            cancellable = true)
    private void projecte$renderInventoryModel(FontRenderer fontRenderer, TextureManager textureManager,
                                               ItemStack stack, int x, int y, CallbackInfo ci) {
        if (stack == null || stack.getItem() == null) {
            return;
        }
        IItemRenderer renderer = MinecraftForgeClient.getItemRenderer(stack.getItem());
        if (renderer != null && renderer.handleRenderType(stack, IItemRenderer.ItemRenderType.INVENTORY)) {
            renderer.renderItem(IItemRenderer.ItemRenderType.INVENTORY, stack, fontRenderer, textureManager, x, y);
            // Balance the pushMatrix that MITE opens for the 3D block render.
            GL11.glPopMatrix();
            ci.cancel();
        }
    }
}
