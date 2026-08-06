package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.Block;
import net.minecraft.ItemPickaxe;
import net.minecraft.ItemTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes every MITE pickaxe (ItemPickaxe subclasses) effective against the
 * transmutation table, so using a pickaxe accelerates mining it (the tool's
 * normal harvest efficiency applies instead of the bare-hand speed). The
 * block keeps its custom non-tool material, so bare-hand mining still works
 * at the base speed.
 */
@Mixin(ItemTool.class)
public abstract class ProjectETransmutationTablePickaxeMixin
{
	@Inject(method = "isEffectiveAgainstBlock", at = @At("HEAD"), cancellable = true)
	private void projecte$transmutationTableEffectiveForPickaxe(Block block, int metadata, CallbackInfoReturnable<Boolean> cir)
	{
		if ((Object) this instanceof ItemPickaxe && block == ObjHandler.transmuteStone)
		{
			cir.setReturnValue(true);
		}
	}
}