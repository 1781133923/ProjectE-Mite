package moze_intel.projecte.gameObjs.customRecipes;

import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.InventoryCrafting;
import net.minecraft.Item;
import net.minecraft.ItemArmor;
import net.minecraft.ItemAxe;
import net.minecraft.ItemBow;
import net.minecraft.ItemFishingRod;
import net.minecraft.ItemFlintAndSteel;
import net.minecraft.ItemHoe;
import net.minecraft.ItemPickaxe;
import net.minecraft.ItemShears;
import net.minecraft.ItemSpade;
import net.minecraft.ItemStack;
import net.minecraft.ItemSword;
import net.minecraft.ItemTool;
import moze_intel.projecte.compat.MITERecipeAdapter;
import net.minecraft.World;

public class RecipesCovalenceRepair extends MITERecipeAdapter
{
	private ItemStack output;

	@Override
	public boolean matches(InventoryCrafting inv, World world) 
	{
		ItemStack[] dust = new ItemStack[8];
		ItemStack tool = null;
		boolean foundItem = false;
		int dustCounter = 0;
		
		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack input = inv.getStackInSlot(i);
			
			if (input == null)
			{
				continue;
			}
			
			if (isItemRepairable(input))
			{
				if (!foundItem)
				{
					tool = input;
					foundItem = true;
				}
				else
				{
					return false;
				}
			}
			else if (input.getItem() == ObjHandler.covalence)
			{
				if (dustCounter < 8)
				{
					dust[dustCounter] = input;
					dustCounter++;
				}
				else
				{
					return false;
				}
			}
		}
		
		if (tool == null || !foundItem || dustCounter == 0)
		{
			return false;
		}

		if (!correctDustCount(dustCounter, tool.getItem()))
		{
			return false;
		}

		int dustDamage = getDustType(tool);

		for (ItemStack stack : dust) {
			if (stack != null && stack.getItemDamage() < dustDamage) {
				return false;
			}
		}
		
		output = tool.copy();
		output.setItemDamage(0);
		return true;
	}

	private boolean correctDustCount(int dustCounter, Item toRepair)
	{
		if (toRepair instanceof ItemSpade || toRepair instanceof ItemShears
				|| toRepair instanceof ItemFlintAndSteel || toRepair instanceof ItemFishingRod)
		{
			return dustCounter == 1;
		}

		if (toRepair instanceof ItemSword)
		{
			return dustCounter == 2;
		}

		if (toRepair instanceof ItemAxe || toRepair instanceof ItemPickaxe || toRepair instanceof ItemBow)
		{
			return dustCounter == 3;
		}

		if (toRepair instanceof ItemArmor)
		{
			ItemArmor armor = ((ItemArmor) toRepair);
			switch(armor.armorType)
			{
				case 0: return dustCounter == 5;
				case 1: return dustCounter == 8;
				case 2: return dustCounter == 7;
				case 3: return dustCounter == 4;
				default: return false;
			}
		}

		return dustCounter == 3;

	}

	private boolean isItemRepairable(ItemStack stack)
	{
		if (stack.getHasSubtypes())
		{
			return false;
		}

		if (stack.getMaxDamage() == 0 || stack.getItemDamage() == 0)
		{
			return false;
		}
		
		Item item = stack.getItem();

		if (item instanceof ItemShears || item instanceof ItemFlintAndSteel || item instanceof ItemFishingRod || item instanceof ItemBow)
		{
			return true;
		}

		return (item instanceof ItemTool || item instanceof ItemSword || item instanceof ItemHoe || item instanceof ItemArmor);
	}
	
	private int getDustType(ItemStack stack)
	{
		Item item = stack.getItem();
		
		if (item instanceof ItemShears || item instanceof ItemFlintAndSteel)
		{
			return 1;
		}

		if (item instanceof ItemBow || item instanceof ItemFishingRod)
		{
			return 0;
		}
		
		String name = "";
		
		if (item instanceof ItemTool)
		{
			name = ((ItemTool) item).getToolMaterialName();
		}
		else if (item instanceof ItemSword)
		{
			name = ((ItemSword) item).getToolMaterialName();
		}
		else if (item instanceof ItemHoe)
		{
			name = ((ItemHoe) item).getToolMaterialName();
		}
		else if (item instanceof ItemArmor)
		{
			name = ((ItemArmor) item).getArmorMaterial().toString();
		}
		
		if (name.equals("WOOD") || name.equals("STONE") || name.equals("CLOTH"))
		{
			return 0;
		}

		if (name.equals("IRON") || name.equals("GOLD") || name.equals("CHAIN"))
		{
			return 1;
		}

		return 2;
	}
	
	@Override
	public ItemStack getCraftingOutput(InventoryCrafting var1) 
	{
		return output.copy();
	}

	@Override
	public int getRecipeSize() 
	{
		return 10;
	}

	@Override
	public ItemStack getRecipeOutput() 
	{
		return output;
	}
}
