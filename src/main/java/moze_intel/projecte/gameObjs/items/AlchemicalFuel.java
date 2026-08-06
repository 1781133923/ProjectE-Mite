package moze_intel.projecte.gameObjs.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.IconRegister;
import net.minecraft.CreativeTabs;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Icon;
import net.minecraft.MathHelper;

import java.util.List;

public class AlchemicalFuel extends ItemPE
{
	private final String[] names = new String[] {"alchemical_coal", "mobius", "aeternalis"};
	@SideOnly(Side.CLIENT)
	private Icon[] icons;
	
	public AlchemicalFuel()
	{
		this.setUnlocalizedName("fuel");
		this.setSubtypes(3);
	}

	// Original ProjectE burn times, scaled from vanilla coal (1600 ticks):
	// alchemical coal = 4x coal, mobius = 4x alchemical, aeternalis = 4x mobius.
	public static final int ALCH_BURN_TIME = 6400;
	public static final int MOBIUS_BURN_TIME = 25600;
	public static final int AETERNALIS_BURN_TIME = 102400;

	@Override
	public int getBurnTime(ItemStack stack)
	{
		switch (stack == null ? -1 : stack.getItemSubtype())
		{
			case 0:
				return ALCH_BURN_TIME;
			case 1:
				return MOBIUS_BURN_TIME;
			case 2:
				return AETERNALIS_BURN_TIME;
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
		if (stack.getItemSubtype() > 2)
		{
			return "pe.debug.metainvalid";
		}

		return super.getUnlocalizedName()+ "_" + names[MathHelper.clamp_int(stack.getItemSubtype(), 0, 2)];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(int itemID, CreativeTabs cTab, List list)
	{
		for (int i = 0; i < 3; ++i)
			list.add(new ItemStack(this, 1, i));
	}
	
	@SideOnly(Side.CLIENT)
	public Icon getIconFromSubtype(int par1)
	{
		return icons[MathHelper.clamp_int(par1, 0, 2)];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		icons = new Icon[3];
		for (int i = 0; i < 3; i++)
			icons[i] = register.registerIcon(this.getTexture("fuels", names[i]));
	}
}
