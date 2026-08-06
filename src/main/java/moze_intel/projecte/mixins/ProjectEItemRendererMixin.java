package moze_intel.projecte.mixins;

import net.minecraft.EntityLivingBase;
import net.minecraft.ItemRenderer;
import net.minecraft.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replace MITE's held-block rendering (RenderBlocks.renderBlockAsItem, which
 * draws a plain textured cube) with ProjectE's own 3D model for blocks that
 * have a registered IItemRenderer. The injection happens at the
 * renderBlockAsItem call, after MITE has already applied the held-item
 * transforms, so the model appears in the hand at the correct pose.
 */
@Mixin(ItemRenderer.class)
public abstract class ProjectEItemRendererMixin {
    @Inject(method = "renderItem(Lnet/minecraft/EntityLivingBase;Lnet/minecraft/ItemStack;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/RenderBlocks;renderBlockAsItem(Lnet/minecraft/Block;IF)V"),
            cancellable = true)
    private void projecte$renderHeldModel(EntityLivingBase entity, ItemStack stack, int par3, CallbackInfo ci) {
        if (stack == null || stack.getItem() == null) {
            return;
        }
        IItemRenderer renderer = MinecraftForgeClient.getItemRenderer(stack.getItem());
        if (renderer != null
                && renderer.handleRenderType(stack, IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON)) {
            renderer.renderItem(IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON, stack);
            // Balance the pushMatrix at the top of ItemRenderer.renderItem.
            GL11.glPopMatrix();
            ci.cancel();
        }
    }
}
