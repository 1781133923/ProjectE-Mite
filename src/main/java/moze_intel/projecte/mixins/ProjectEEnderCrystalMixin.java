package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.tools.PEToolBase;
import moze_intel.projecte.gameObjs.items.tools.RedKatar;
import net.minecraft.DamageSource;
import net.minecraft.EntityEnderCrystal;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE end crystals are only damageable by melee from an ItemTool effective
 * against mithril block; ProjectE tools extend ItemMode instead, so they were
 * all immune. Let mining weapons (pickaxe / hammer / morning star) and the
 * red matter katar damage the crystals.
 */
@Mixin(EntityEnderCrystal.class)
public abstract class ProjectEEnderCrystalMixin
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