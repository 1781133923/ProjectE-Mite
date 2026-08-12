package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.tools.PEToolBase;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityDragon;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The ender dragon only takes damage through its body parts
 * (EntityDragonPart -> attackEntityFromPart -> func_82195_e); the main entity
 * attackEntityFrom returns null, so ProjectE AOE special attacks (which hit
 * the main entity instead of a part) dealt no damage. Route direct hits from
 * players wielding a ProjectE tool through the same part damage path.
 */
@Mixin(EntityDragon.class)
public abstract class ProjectEDragonDamageMixin
{
	@Shadow
	protected abstract EntityDamageResult func_82195_e(Damage damage);

	@Inject(method = "attackEntityFrom", at = @At("HEAD"), cancellable = true)
	private void projecte$routePEToolHits(Damage damage, CallbackInfoReturnable<EntityDamageResult> cir)
	{
		DamageSource source = damage.getSource();
		if (source == null)
		{
			return;
		}
		Entity responsible = source.getResponsibleEntity();
		if (!(responsible instanceof EntityPlayer))
		{
			return;
		}
		ItemStack held = ((EntityPlayer) responsible).getHeldItemStack();
		if (held != null && held.getItem() instanceof PEToolBase)
		{
			cir.setReturnValue(this.func_82195_e(damage));
		}
	}
}