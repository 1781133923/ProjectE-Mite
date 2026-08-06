package moze_intel.projecte.mixins;

import net.minecraft.BlockCrops;
import net.minecraft.BlockCropsDead;
import net.minecraft.Icon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE bug: dead crops only have a small icon array (num_growth_stages, e.g. 3)
 * but their metadata keeps the full growth value of the crop they came from
 * (0-7). When a crop dies at a late growth stage, BlockCropsDead.getIcon
 * indexes iconArray with growth stage 3+ and the renderer throws
 * ArrayIndexOutOfBoundsException, crashing the client while rendering the
 * world. Clamp the stage to the last valid dead-crop icon.
 */
@Mixin(BlockCropsDead.class)
public abstract class ProjectECropsDeadIconMixin
{
	@Inject(method = "getIcon", at = @At("HEAD"), cancellable = true)
	private void projecte$clampDeadCropIcon(int side, int metadata, CallbackInfoReturnable<Icon> cir)
	{
		try
		{
			int growth = ((BlockCrops) (Object) this).getGrowthStage(metadata);
			// num_growth_stages / iconArray live on the parent BlockCrops, so
			// @Shadow cannot see them here - read them reflectively, only in
			// the (rare) path where the growth stage actually overflows.
			java.lang.reflect.Field stagesField = BlockCrops.class.getDeclaredField("num_growth_stages");
			stagesField.setAccessible(true);
			int stages = stagesField.getInt(this);
			if (stages > 0 && growth >= stages)
			{
				java.lang.reflect.Field iconsField = BlockCrops.class.getDeclaredField("iconArray");
				iconsField.setAccessible(true);
				Icon[] icons = (Icon[]) iconsField.get(this);
				cir.setReturnValue(icons[stages - 1]);
			}
		}
		catch (Throwable t)
		{
			// Never let the clamp itself break rendering.
		}
	}
}
