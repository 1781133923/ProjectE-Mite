package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.tools.PEToolBase;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityDamageSource;
import net.minecraft.EntityLivingBase;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE has creatures (e.g. wights) that are immune to everything except
 * damage with a magic aspect, which vanilla only grants when the attacker's
 * weapon stack is enchanted. ProjectE's dark/red matter weapons are magical
 * by nature, so make melee damage from them count as magic too - no
 * enchanting required (and no enchant glint added).
 */
@Mixin(EntityDamageSource.class)
public abstract class ProjectEMagicWeaponMixin extends DamageSource
{
	protected ProjectEMagicWeaponMixin(String par1)
	{
		super(par1);
	}

	@Inject(method = "<init>(Ljava/lang/String;Lnet/minecraft/Entity;Lnet/minecraft/Entity;)V", at = @At("TAIL"))
	private void projecte$treatProjectEWeaponsAsEnchanted(String par1, Entity par2, Entity par3, CallbackInfo ci)
	{
		if (par3 instanceof EntityLivingBase)
		{
			ItemStack stack = ((EntityLivingBase) par3).getHeldItemStack();
			if (stack != null && stack.getItem() instanceof PEToolBase && !this.hasMagicAspect())
			{
				this.setMagicAspect();
			}
		}
	}
}
