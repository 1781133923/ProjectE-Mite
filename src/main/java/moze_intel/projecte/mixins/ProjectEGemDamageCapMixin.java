package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.armor.GemArmorBase;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Full gem armour set bonus: damage cap. MITE applies every defence modifier
 * (armour, absorption, minimum 1) before the only setHealth call inside
 * attackEntityFromHelper; every hit is capped at 1/4 of the player's
 * full health, so repeated hits without healing still kill. Only players
 * benefit.
 */
@Mixin(EntityLivingBase.class)
public abstract class ProjectEGemDamageCapMixin
{
	@ModifyArg(method = "attackEntityFromHelper",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityLivingBase;setHealth(F)V"),
			index = 0)
	private float projecte$capOneShotDamage(float newHealth)
	{
		Object self = this;
		if (!(self instanceof EntityPlayer))
		{
			return newHealth;
		}
		EntityPlayer player = (EntityPlayer) self;
		if (!GemArmorBase.hasFullSet(player))
		{
			return newHealth;
		}
		float oldHealth = player.getHealth();
		float maxHealth = player.getMaxHealth();
		float cap = maxHealth * 0.25F;
		float damage = oldHealth - newHealth;
		if (damage > cap)
		{
			// Cap every hit at 1/4 of full health; repeated hits without
			// healing still kill.
			return oldHealth - cap;
		}
		return newHealth;
	}
}