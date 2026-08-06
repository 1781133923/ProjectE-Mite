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

public class CovalenceDust extends ItemPE
{
	private final String[] names = new String[] {"low", "medium", "high"};
	@SideOnly(Side.CLIENT)
	private Icon[] icons;
	
	public CovalenceDust()
	{
		this.setUnlocalizedName("covalence_dust");
		this.setSubtypes(3);
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
			icons[i] = register.registerIcon(this.getTexture("covalence_dust", names[i]));
	}
}
