package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.tools.PEToolBase;
import net.minecraft.InventoryCrafting;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ProjectE tools encode their charge in the item-damage field (the durability
 * bar doubles as the charge bar), so a charged tool looks "damaged" to MITE's
 * crafting check: InventoryCrafting.hasDamagedItem drives both the GUI red-X
 * and SlotCrafting's refusal to craft. Treat ProjectE tools as undamaged in
 * the crafting matrix while still flagging genuinely damaged non-ProjectE
 * ingredients.
 */
@Mixin(InventoryCrafting.class)
public abstract class ProjectECraftingDamageMixin
{
	@Inject(method = "hasDamagedItem", at = @At("HEAD"), cancellable = true)
	private void projecte$ignorePEToolDamage(CallbackInfoReturnable<Boolean> cir)
	{
		InventoryCrafting self = (InventoryCrafting) (Object) this;
		for (int i = 0; i < self.getSizeInventory(); i++)
		{
			ItemStack stack = self.getStackInSlot(i);
			if (stack == null || stack.getItem() instanceof PEToolBase)
			{
				continue;
			}
			if (stack.isItemDamaged())
			{
				cir.setReturnValue(true);
				return;
			}
		}
		cir.setReturnValue(false);
	}
}