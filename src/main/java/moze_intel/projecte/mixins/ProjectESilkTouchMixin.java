package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.tools.PEToolBase;
import net.minecraft.Block;
import net.minecraft.EnchantmentHelper;
import net.minecraft.EntityLivingBase;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE decides silk-touch drops in EntityLivingBase.canSilkHarvestBlock: the
 * block must allow silk, the held item must carry the enchantment, and the
 * held stack must be an ItemTool effective against the block
 * (ItemStack.isTool() -> instanceof ItemTool). ProjectE tools extend
 * ItemMode instead of ItemTool, so silk touch never fired. Mirror the same
 * gate with PEToolBase.isEffectiveAgainstBlock (ores/stone work like a
 * vanilla pick; wood blocks like bookshelves stay non-silk, matching MITE).
 */
@Mixin(EntityLivingBase.class)
public abstract class ProjectESilkTouchMixin
{
	@Inject(method = "canSilkHarvestBlock", at = @At("HEAD"), cancellable = true)
	private void projecte$allowPESilkHarvest(Block block, int metadata, CallbackInfoReturnable<Boolean> cir)
	{
		EntityLivingBase self = (EntityLivingBase) (Object) this;
		ItemStack held = self.getHeldItemStack();
		if (held != null && held.getItem() instanceof PEToolBase)
		{
			PEToolBase tool = (PEToolBase) held.getItem();
			if (block != null
					&& block.canSilkHarvest(metadata)
					&& EnchantmentHelper.getSilkTouchModifier(self)
					&& tool.isEffectiveAgainstBlock(block, metadata))
			{
				cir.setReturnValue(true);
			}
		}
	}
}