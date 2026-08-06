package moze_intel.projecte.gameObjs.items.itemBlocks;

import net.minecraft.Block;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;

public class ItemFuelBlock extends ItemBlock
{
	public ItemFuelBlock(Block block) 
	{
		super(block);
			}

	@Override
	public int getBurnTime(ItemStack stack)
	{
		// A fuel block burns 10x as long as a single piece of that fuel.
		switch (stack == null ? -1 : stack.getItemSubtype())
		{
			case 0:
				return moze_intel.projecte.gameObjs.items.AlchemicalFuel.ALCH_BURN_TIME * 10;
			case 1:
				return moze_intel.projecte.gameObjs.items.AlchemicalFuel.MOBIUS_BURN_TIME * 10;
			case 2:
				return moze_intel.projecte.gameObjs.items.AlchemicalFuel.AETERNALIS_BURN_TIME * 10;
			default:
				return 0;
		}
	}

	@Override
	public int getHeatLevel(ItemStack stack)
	{
		switch (stack == null ? -1 : stack.getItemSubtype())
		{
			case 0:
				return net.minecraft.TileEntityFurnace.HEAT_LEVEL_COAL;
			case 1:
				return net.minecraft.TileEntityFurnace.HEAT_LEVEL_LAVA;
			case 2:
				return net.minecraft.TileEntityFurnace.HEAT_LEVEL_BLAZE_ROD;
			default:
				return 0;
		}
	}

	@Override
	public boolean canBurnAsFuelSource()
	{
		return true;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if (stack == null)
		{
			return super.getUnlocalizedName();
		}
		switch (stack.getItemSubtype())
		{
			case 0:
				return "tile.pe_fuel_block_0";
			case 1:
				return "tile.pe_fuel_block_1";
			case 2:
				return "tile.pe_fuel_block_2";
			default:
				return "tile.pe_fuel_block_null";
		}
	}
	
	@Override
	public int getMetadata(int meta)
	{
		return meta;
	}
}
