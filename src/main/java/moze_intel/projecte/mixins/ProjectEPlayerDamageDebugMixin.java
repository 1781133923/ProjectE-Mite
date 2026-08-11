package moze_intel.projecte.mixins;

import net.minecraft.Damage;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TEMP DEBUG (zombie boss damage investigation): logs damage the player
 * receives from Extreme's EntityZombieBoss - raw amount, damage type, actual
 * health lost after armor/protection/special reduction, and player HP.
 */
@Mixin(EntityPlayer.class)
public abstract class ProjectEPlayerDamageDebugMixin
{
	private static final String ZOMBIE_BOSS_CLASS = "cn.wensc.mitemod.extreme.entity.EntityZombieBoss";

	@Inject(method = "attackEntityFrom", at = @At("HEAD"), require = 0)
	private void projecte$debugPlayerHitHead(Damage damage, CallbackInfoReturnable<EntityDamageResult> cir)
	{
		if (!isZombieBossSource(damage))
		{
			return;
		}
		EntityPlayer self = (EntityPlayer) (Object) this;
		System.out.println("[ProjectE][boss-debug] boss-hit-player raw=" + damage.getAmount()
				+ " type=" + damage.getSource().damageType
				+ " playerHp=" + self.getHealth()
				);
	}

	@Inject(method = "attackEntityFrom", at = @At("RETURN"), require = 0)
	private void projecte$debugPlayerHitReturn(Damage damage, CallbackInfoReturnable<EntityDamageResult> cir)
	{
		if (!isZombieBossSource(damage))
		{
			return;
		}
		EntityPlayer self = (EntityPlayer) (Object) this;
		EntityDamageResult result = cir.getReturnValue();
		float lost = result != null ? result.getAmountOfHealthLost() : -1.0F;
		System.out.println("[ProjectE][boss-debug] after-boss-hit raw=" + damage.getAmount()
				+ " lost=" + lost
				+ " reduction=" + (damage.getAmount() - lost)
				+ " armorAffected=" + (result != null && result.entityArmorWasAffected())
				+ " playerHp=" + self.getHealth()
				);
	}

	private static boolean isZombieBossSource(Damage damage)
	{
		if (damage == null || damage.getSource() == null)
		{
			return false;
		}
		Entity src = damage.getSource().getResponsibleEntity();
		return src != null && ZOMBIE_BOSS_CLASS.equals(src.getClass().getName());
	}
}