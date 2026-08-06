package moze_intel.projecte.mixins;

import net.minecraft.Block;
import net.minecraft.Minecraft;
import net.minecraft.RenderBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MITE builds some RenderBlocks instances (RenderItem, RenderEnderman, ...)
 * during RenderManager class initialisation, i.e. before Minecraft.theMinecraft
 * has been assigned, so their private final minecraftRB field stays null. The
 * ambient-occlusion partial-render-bounds check then throws an NPE whenever a
 * non-standard-form block is rendered (a dropped chest/condenser/pedestal item
 * entity, or an enderman carrying a block). Route those field reads to the live
 * Minecraft instance instead.
 */
@Mixin(RenderBlocks.class)
public abstract class ProjectERenderBlocksMixin
{
	@Redirect(method = {
			"setRenderBounds(DDDDDD)V",
			"setRenderBoundsForNonStandardFormBlock(Lnet/minecraft/Block;)V",
			"XXXsetRenderBoundsFromBlock(Lnet/minecraft/Block;)V",
			"overrideBlockBounds(DDDDDD)V"
	}, at = @At(value = "FIELD",
			target = "Lnet/minecraft/RenderBlocks;minecraftRB:Lnet/minecraft/Minecraft;"))
	private Minecraft projecte$getMinecraftRB(RenderBlocks self)
	{
		return Minecraft.getMinecraft();
	}
}
