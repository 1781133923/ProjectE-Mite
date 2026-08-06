package moze_intel.projecte.gameObjs.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.utils.AchievementHandler;
import net.minecraft.IconRegister;
import net.minecraft.CreativeTabs;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Icon;
import net.minecraft.MathHelper;
import net.minecraft.World;

import java.util.List;

public class Matter extends ItemPE 
{
	private final String[] names = new String[] {"dark", "red"};
	@SideOnly(Side.CLIENT)
	private Icon[] icons;
	
	public Matter()
	{
		this.setUnlocalizedName("matter");
		this.setSubtypes(2);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{	
		if (stack == null)
		{
			return super.getUnlocalizedName();
		}
		return super.getUnlocalizedName() + "_" + names[stack.getItemSubtype()];
	}
	
	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) 
	{
		super.onCreated(stack, world, player);
		
		if (!world.isRemote)
		{
			if (stack.getItemSubtype() == 0)
			{
				player.addStat(AchievementHandler.DARK_MATTER, 1);
			}
			else
			{
				player.addStat(AchievementHandler.RED_MATTER, 1);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(int itemID, CreativeTabs cTab, List list)
	{
		for (int i = 0; i < 2; i++)
		{
			list.add(new ItemStack(this, 1, i));
		}
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
		icons = new Icon[2];
		
		for (int i = 0; i < 2; i++)
		{
			icons[i] = register.registerIcon(this.getTexture("matter", names[i]));
		}
	}
}
