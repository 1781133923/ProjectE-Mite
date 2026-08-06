package moze_intel.projecte.mixins;

import moze_intel.projecte.handlers.PlayerChecks;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE derives fire/lava immunity from EntityLivingBase#isHarmedByFire /
 * isHarmedByLava instead of the 1.7.10 isImmuneToFire field. Give players
 * holding/wearing a ProjectE fire protector (volcanite amulet) immunity
 * through those hooks.
 */
@Mixin(EntityLivingBase.class)
public abstract class ProjectEFireImmunityMixin
{
	@Inject(method = "isHarmedByFire", at = @At("HEAD"), cancellable = true)
	private void projecte$fireImmunity(CallbackInfoReturnable<Boolean> cir)
	{
		Object self = this;
		if (self instanceof EntityPlayer && PlayerChecks.shouldPlayerResistFire((EntityPlayer) self))
		{
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "isHarmedByLava", at = @At("HEAD"), cancellable = true)
	private void projecte$lavaImmunity(CallbackInfoReturnable<Boolean> cir)
	{
		Object self = this;
		if (self instanceof EntityPlayer && PlayerChecks.shouldPlayerResistFire((EntityPlayer) self))
		{
			cir.setReturnValue(false);
		}
	}
}
