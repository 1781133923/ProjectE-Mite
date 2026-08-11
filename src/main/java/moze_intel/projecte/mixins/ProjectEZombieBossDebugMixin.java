package moze_intel.projecte.mixins;

import net.minecraft.Damage;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TEMP DEBUG (zombie boss damage investigation): logs the player's damage
 * dealt to Extreme's EntityZombieBoss - raw amount, damage type, weapon,
 * actual health lost after the boss's own reduction, and boss HP. Uses a
 * string mixin target so the mod stays soft-dependent on Extreme (absent ->
 * mixin simply not applied).
 */
@Mixin(targets = {"cn.wensc.mitemod.extreme.entity.EntityZombieBoss"})
public abstract class ProjectEZombieBossDebugMixin
{
	@Inject(method = "attackEntityFrom", at = @At("HEAD"), require = 0)
	private void projecte$debugBossHitHead(Damage damage, CallbackInfoReturnable<EntityDamageResult> cir)
	{
		Entity source = damage.getSource().getResponsibleEntity();
		if (!(source instanceof EntityPlayer))
		{
			return;
		}
		EntityLivingBase boss = (EntityLivingBase) (Object) this;
		System.out.println("[ProjectE][boss-debug] player-hit-boss raw=" + damage.getAmount()
				+ " type=" + damage.getSource().damageType
				+ " weapon=" + (damage.getItemAttackedWith() != null ? damage.getItemAttackedWith().getItem().getClass().getSimpleName() : "none")
				+ " bossHp=" + boss.getHealth());
	}

	@Inject(method = "attackEntityFrom", at = @At("RETURN"), require = 0)
	private void projecte$debugBossHitReturn(Damage damage, CallbackInfoReturnable<EntityDamageResult> cir)
	{
		Entity source = damage.getSource().getResponsibleEntity();
		if (!(source instanceof EntityPlayer))
		{
			return;
		}
		EntityLivingBase boss = (EntityLivingBase) (Object) this;
		EntityDamageResult result = cir.getReturnValue();
		float lost = result != null ? result.getAmountOfHealthLost() : -1.0F;
		System.out.println("[ProjectE][boss-debug] after-player-hit raw=" + damage.getAmount()
				+ " lost=" + lost
				+ " reduction=" + (damage.getAmount() - lost)
				+ " armorAffected=" + (result != null && result.entityArmorWasAffected())
				+ " destroyed=" + (result != null && result.entityWasDestroyed())
				+ " bossHp=" + boss.getHealth());
	}
}