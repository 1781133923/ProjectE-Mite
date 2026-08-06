package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.armor.GemArmorBase;
import moze_intel.projecte.gameObjs.items.armor.DMArmor;
import moze_intel.projecte.gameObjs.items.armor.RMArmor;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Full matter armour set bonus: after the armour value (and any other defence
 * modifiers) have already reduced incoming physical damage, cut the remaining
 * damage further (dark matter 20%, red matter 40%, gem 60%). MITE applies all defence modifiers inside
 * Damage.applyTargetDefenseModifiers (called from attackEntityFromHelper), so
 * this hook runs exactly at "after armour reduction". Magic, fire, explosion,
 * fall, drowning, starvation, poison, acid and absolute damage are excluded -
 * they have their own rules (magic has its own per-set bonus, gem boots give
 * fall immunity, ...). Only players benefit; a mob wearing the set still only
 * gets the raw armour value.
 */
@Mixin(EntityLivingBase.class)
public abstract class ProjectEGemPhysicalResistMixin
{
	@Inject(method = "attackEntityFromHelper",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/Damage;applyTargetDefenseModifiers(Lnet/minecraft/EntityLivingBase;Lnet/minecraft/EntityDamageResult;)F",
					shift = At.Shift.AFTER))
	private void projecte$gemFullSetPhysicalResist(Damage damage, EntityDamageResult result,
			CallbackInfoReturnable<EntityDamageResult> cir)
	{
		Object self = this;
		if (!(self instanceof EntityPlayer))
		{
			return;
		}
		EntityPlayer player = (EntityPlayer) self;
		float reduction = getFullSetPhysicalReduction(player);
		if (reduction <= 0.0F)
		{
			return;
		}
		DamageSource source = damage.getSource();
		if (source == null || !isPhysicalDamage(source))
		{
			return;
		}
		damage.setAmount(damage.getAmount() * (1.0F - reduction));
	}

	private static float getFullSetPhysicalReduction(EntityPlayer player)
	{
		if (GemArmorBase.hasFullSet(player))
		{
			return 0.6F; // 60%
		}
		if (RMArmor.hasFullSet(player))
		{
			return 0.4F; // 40%
		}
		if (DMArmor.hasFullSet(player))
		{
			return 0.2F; // 20%
		}
		return 0.0F;
	}

	private static boolean isPhysicalDamage(DamageSource source)
	{
		return !source.hasMagicAspect()
				&& source != DamageSource.wither
				&& !source.isFireDamage()
				&& !source.isLavaDamage()
				&& !source.isExplosion()
				&& !source.isFallDamage()
				&& !source.isDrowning()
				&& !source.isStarving()
				&& !source.isPoison()
				&& !source.isAcidDamage()
				&& !source.isPepsinDamage()
				&& !source.isSunlight()
				&& !source.isAbsolute();
	}
}
