package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.tools.PEToolBase;
import moze_intel.projecte.gameObjs.items.tools.RedKatar;
import net.minecraft.DamageSource;
import net.minecraft.EntityMagmaCube;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE magma cubes are only damageable by stone-effective ItemTools (plus
 * water/snowball/explosion); ProjectE tools extend ItemMode instead, so they
 * fell into the "immune" branch. Let mining weapons (pickaxe / hammer /
 * morning star) and the red matter katar damage magma cubes.
 */
@Mixin(EntityMagmaCube.class)
public abstract class ProjectEMagmaCubeDamageMixin
{
	@Inject(method = "isImmuneTo", at = @At("HEAD"), cancellable = true)
	private void projecte$allowPETools(DamageSource source, CallbackInfoReturnable<Boolean> cir)
	{
		ItemStack stack = source.getItemAttackedWith();
		if (stack == null || !(stack.getItem() instanceof PEToolBase))
		{
			return;
		}
		PEToolBase tool = (PEToolBase) stack.getItem();
		if (tool.isMiningWeapon() || tool instanceof RedKatar)
		{
			cir.setReturnValue(false);
		}
	}
}