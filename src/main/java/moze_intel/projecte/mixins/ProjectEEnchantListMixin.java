package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.tools.PEToolBase;
import net.minecraft.Enchantment;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MITE builds the enchanting-table pool in
 * EnchantmentHelper.mapEnchantmentData(int, ItemStack) by filtering every
 * enchantment through Enchantment.canEnchantItem(Item), and every MITE
 * enchantment hardcodes that check as instanceof/exact-class against
 * ItemSword, ItemWarHammer, ItemPickaxe, ItemTool... ProjectE tools extend
 * ItemMode instead of ItemTool, so they report a working enchantability but
 * never qualify for any weapon/tool enchantment. Redirect the single filter
 * call and map ProjectE tool classes onto their MITE-equivalent pools.
 */
@Mixin(EnchantmentHelper.class)
public abstract class ProjectEEnchantListMixin
{
	@Redirect(method = "mapEnchantmentData", at = @At(value = "INVOKE", target = "Lnet/minecraft/Enchantment;canEnchantItem(Lnet/minecraft/Item;)Z"))
	private static boolean projecte$allowPEEnchant(Enchantment enchantment, Item item)
	{
		if (item instanceof PEToolBase && projecte$isPEEnchantable(enchantment, (PEToolBase) item))
		{
			return true;
		}
		return enchantment.canEnchantItem(item);
	}

	private static boolean projecte$isPEEnchantable(Enchantment enchantment, PEToolBase tool)
	{
		String cls = tool.getPrimaryToolClass();
		boolean sword = "sword".equals(cls) || "katar".equals(cls);
		boolean hammer = "hammer".equals(cls) || "morning_star".equals(cls);

		if (sword)
		{
			return enchantment == Enchantment.sharpness
					|| enchantment == Enchantment.baneOfArthropods
					|| enchantment == Enchantment.fireAspect
					|| enchantment == Enchantment.looting
					|| enchantment == Enchantment.disarming
					|| enchantment == Enchantment.vampiric;
		}
		if (hammer)
		{
			return enchantment == Enchantment.smite
					|| enchantment == Enchantment.knockback
					|| enchantment == Enchantment.stun
					|| enchantment == Enchantment.unbreaking;
		}
		if ("pickaxe".equals(cls))
		{
			return enchantment == Enchantment.efficiency
					|| enchantment == Enchantment.fortune
					|| enchantment == Enchantment.silkTouch
					|| enchantment == Enchantment.unbreaking
					|| enchantment == Enchantment.piercing;
		}
		if ("shovel".equals(cls))
		{
			return enchantment == Enchantment.efficiency
					|| enchantment == Enchantment.fortune
					|| enchantment == Enchantment.silkTouch
					|| enchantment == Enchantment.unbreaking;
		}
		if ("axe".equals(cls))
		{
			return enchantment == Enchantment.efficiency
					|| enchantment == Enchantment.unbreaking
					|| enchantment == Enchantment.tree_felling;
		}
		if ("hoe".equals(cls))
		{
			return enchantment == Enchantment.efficiency
					|| enchantment == Enchantment.unbreaking
					|| enchantment == Enchantment.harvesting
					|| enchantment == Enchantment.fertility;
		}
		if ("shears".equals(cls))
		{
			return enchantment == Enchantment.silkTouch;
		}
		return false;
	}
}