package moze_intel.projecte.gameObjs.customRecipes;

import net.minecraft.CraftingResult;
import net.minecraft.InventoryCrafting;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;

import java.util.List;

/**
 * Shapeless upgrade recipe that copies the source (lower-tier) piece's NBT
 * onto the upgraded result. MITE's generic propagateTagCompound copies the
 * FIRST tag-carrying ingredient, which is unreliable here because other
 * ingredients (Klein Stars, rings) also carry NBT - the armor's tool level,
 * exp, modifiers and forging grade would be lost.
 */
public class PEUpgradeShapelessRecipe extends net.minecraft.ShapelessRecipes
{
	private final Item sourceItem;

	public PEUpgradeShapelessRecipe(ItemStack output, List input, Item sourceItem)
	{
		super(output, input, false);
		this.sourceItem = sourceItem;
	}

	@Override
	public CraftingResult getCraftingResult(InventoryCrafting inv)
	{
		ItemStack result = getRecipeOutput().copy();
		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack s = inv.getStackInSlot(i);
			if (s != null && s.getItem() == sourceItem && s.hasTagCompound())
			{
				result.setTagCompound((NBTTagCompound) s.stackTagCompound.copy());
				break;
			}
		}
		return new CraftingResult(result, getUnmodifiedDifficulty(), getSkillsets(), this);
	}
}