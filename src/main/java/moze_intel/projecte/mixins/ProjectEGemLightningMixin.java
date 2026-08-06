package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.armor.GemArmorBase;
import net.minecraft.Entity;
import net.minecraft.EntityLightningBolt;
import net.minecraft.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Full gem armour set bonus: real lightning strikes are completely nullified
 * for players. MITE implements being struck by lightning as 5 fire damage plus
 * ignition inside Entity.onStruckByLightning (there is no separate lightning
 * damage source), so cancel the whole strike for a player wearing all four
 * gem pieces. The divine-lightning DamageSource is handled in
 * MITECombatListener.
 */
@Mixin(Entity.class)
public abstract class ProjectEGemLightningMixin
{
	@Inject(method = "onStruckByLightning", at = @At("HEAD"), cancellable = true)
	private void projecte$gemFullSetLightningImmunity(EntityLightningBolt bolt, CallbackInfo ci)
	{
		Object self = this;
		if (self instanceof EntityPlayer && GemArmorBase.hasFullSet((EntityPlayer) self))
		{
			ci.cancel();
		}
	}
}
