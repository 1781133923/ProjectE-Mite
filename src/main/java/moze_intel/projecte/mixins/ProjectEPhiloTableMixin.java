package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.container.PhilosStoneContainer;
import net.minecraft.GuiContainer;
import net.minecraft.ItemStack;
import net.minecraft.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE's crafting GUI marks the crafting result as "shown but prevented" when
 * the recipe requires a workbench tier that the container provides; for any
 * non-workbench crafting container it assumes the low-tier 2x2 grid. The
 * Philosopher's Stone table is an adamantium-tier bench, so force that flag
 * back to false there (the result can always be taken).
 */
@Mixin(GuiContainer.class)
public abstract class ProjectEPhiloTableMixin
{
	@Shadow
	private net.minecraft.Container inventorySlots;

	@Inject(method = "drawItemStackTooltip(Lnet/minecraft/ItemStack;IILnet/minecraft/Slot;)V",
			at = @At("RETURN"))
	private void projecte$philoTableNeverPrevented(ItemStack stack, int x, int y, Slot slot, CallbackInfo ci)
	{
		if (this.inventorySlots instanceof PhilosStoneContainer)
		{
			this.inventorySlots.crafting_result_shown_but_prevented = false;
		}
	}
}
