package moze_intel.projecte.gameObjs.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.utils.AchievementHandler;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.IconRegister;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.Icon;
import net.minecraft.MathHelper;
import net.minecraft.World;

import java.util.List;

public class KleinStar extends ItemPE implements IItemEmc
{
	@SideOnly(Side.CLIENT)
	private Icon[] icons;
	
	public KleinStar()
	{
		this.setUnlocalizedName("klein_star");
		this.setSubtypes(6);
		this.setMaxStackSize(1);
	
	}
	
	public boolean showDurabilityBar(ItemStack stack)
	{
		return stack.hasTagCompound();
	}
	
	public double getDurabilityForDisplay(ItemStack stack)
	{
		double starEmc = getEmc(stack);
		
		if (starEmc == 0)
		{
			return 1.0D;
		}
		
		return 1.0D - starEmc / (double) EMCHelper.getKleinStarMaxEmc(stack);
	}

	
	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		/*if (!world.isRemote)
		{
			this.setEmc(stack, Utils.GetKleinStarMaxEmc(stack));
		}*/
		
		return true;
	}
	
	/*@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) 
	{
		if (!world.isRemote)
		{
			stack.stackTagCompound = new NBTTagCompound();
		}
	}*/
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) 
	{
		if (!stack.hasTagCompound())
		{
			stack.stackTagCompound = new NBTTagCompound();
		}
	}
	
	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) 
	{
		super.onCreated(stack, world, player);
		
		if (!world.isRemote)
		{
		if (stack.getItemSubtype() == 5)
		{
				player.addStat(AchievementHandler.KLEIN_MASTER, 1);
			}
			else
			{
				player.addStat(AchievementHandler.KLEIN_BASIC, 1);
			}
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if (stack == null)
		{
			return super.getUnlocalizedName();
		}
		if (stack.getItemSubtype() > 5)
		{
			return "pe.debug.metainvalid";
		}

		return super.getUnlocalizedName()+ "_" + (stack.getItemSubtype() + 1);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(int itemID, CreativeTabs cTab, List list)
	{
		for (int i = 0; i < 6; ++i)
		{
			list.add(new ItemStack(this, 1, i));
		}
	}
	
	@SideOnly(Side.CLIENT)
	public Icon getIconFromSubtype(int par1)
	{
		return icons[MathHelper.clamp_int(par1, 0, 5)];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		icons = new Icon[6];
		
		for (int i = 0; i < 6; i++)
		{
			icons[i] = register.registerIcon(this.getTexture("stars", "klein_star_"+(i + 1)));
		}
	}

	// -- IItemEmc -- //

	@Override
	public double addEmc(ItemStack stack, double toAdd)
	{
		double add = Math.min(getMaximumEmc(stack) - getStoredEmc(stack), toAdd);
		ItemPE.addEmcToStack(stack, add);
		return add;
	}

	@Override
	public double extractEmc(ItemStack stack, double toRemove)
	{
		double sub = Math.min(getStoredEmc(stack), toRemove);
		ItemPE.removeEmc(stack, sub);
		return sub;
	}

	@Override
	public double getStoredEmc(ItemStack stack)
	{
		return ItemPE.getEmc(stack);
	}

	@Override
	public double getMaximumEmc(ItemStack stack)
	{
		return EMCHelper.getKleinStarMaxEmc(stack);
	}
}
