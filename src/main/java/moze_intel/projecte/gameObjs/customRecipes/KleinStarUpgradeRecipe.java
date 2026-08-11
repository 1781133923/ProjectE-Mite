package moze_intel.projecte.gameObjs.customRecipes;

import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.items.ItemPE;
import net.minecraft.CraftingResult;
import net.minecraft.InventoryCrafting;
import net.minecraft.ItemStack;
import net.minecraft.World;

import java.util.List;

/**
 * Klein Star tier upgrade (4x lower tier -> next tier), registered as a
 * standard ShapelessRecipes so MITE crafts it reliably and recipe viewers
 * (EMI) display it - the previous hidden custom recipe was invisible to
 * recipe UIs. The EMC stored in the four input stars is summed into the
 * upgraded star.
 */
public class KleinStarUpgradeRecipe extends net.minecraft.ShapelessRecipes
{
	private final int sourceTier;

	public KleinStarUpgradeRecipe(ItemStack output, List input, int sourceTier)
	{
		super(output, input, false);
		this.sourceTier = sourceTier;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		int count = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack s = inv.getStackInSlot(i);
			if (s == null)
			{
				continue;
			}
			if (s.getItem() == ObjHandler.kleinStars && s.getItemSubtype() == sourceTier)
			{
				count++;
			}
			else
			{
				return false;
			}
		}
		return count == 4;
	}

	@Override
	public CraftingResult getCraftingResult(InventoryCrafting inv)
	{
		double storedEmc = 0.0D;
		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack s = inv.getStackInSlot(i);
			if (s != null && s.getItem() == ObjHandler.kleinStars)
			{
				storedEmc += ItemPE.getEmc(s);
			}
		}
		ItemStack result = getRecipeOutput().copy();
		if (result.getItem() == ObjHandler.kleinStars)
		{
			ItemPE.setEmc(result, storedEmc);
		}
		return new CraftingResult(result, getUnmodifiedDifficulty(), getSkillsets(), this);
	}
}