package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.tools.PEToolBase;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumEntityReachContext;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ProjectE melee weapons (sword / hammer / katar / morning star) gain +0.5
 * attack reach per charge level. Only the FOR_MELEE_ATTACK reach is boosted;
 * the C-key special attack uses its own charge-based AOE box (attackAOE) and
 * is intentionally unaffected.
 */
@Mixin(EntityPlayer.class)
public abstract class ProjectEMeleeReachMixin
{
	@Inject(method = "getReach(Lnet/minecraft/EnumEntityReachContext;Lnet/minecraft/Entity;)F", at = @At("RETURN"), cancellable = true)
	private void projecte$chargeMeleeReach(EnumEntityReachContext context, Entity target, CallbackInfoReturnable<Float> cir)
	{
		if (context != EnumEntityReachContext.FOR_MELEE_ATTACK)
		{
			return;
		}
		EntityPlayer self = (EntityPlayer) (Object) this;
		ItemStack held = self.getHeldItemStack();
		if (held != null && held.getItem() instanceof PEToolBase)
		{
			PEToolBase tool = (PEToolBase) held.getItem();
			if (tool.gainsChargeReachBonus())
			{
				cir.setReturnValue(cir.getReturnValueF() + 0.5F * tool.getCharge(held));
			}
		}
	}

	/**
	 * Picks / hammers gain +1 block reach per charge while mining (the targeted
	 * block must be one the tool is effective against). Right-click AOE digging
	 * uses its own charge-based box and is unaffected.
	 */
		@Inject(method = "getReach(Lnet/minecraft/Block;I)F", at = @At("RETURN"), cancellable = true)
		private void projecte$chargeBlockMiningReach(net.minecraft.Block block, int metadata, CallbackInfoReturnable<Float> cir)
		{
			EntityPlayer self = (EntityPlayer) (Object) this;
			ItemStack held = self.getHeldItemStack();
			if (held != null && held.getItem() instanceof PEToolBase)
			{
				PEToolBase tool = (PEToolBase) held.getItem();
				if (tool.gainsChargeMiningReachBonus() && tool.isEffectiveAgainstBlock(block, metadata))
				{
					cir.setReturnValue(cir.getReturnValueF() + 1.0F * tool.getCharge(held));
				}
			}
		}
}