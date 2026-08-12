package moze_intel.projecte.gameObjs.items.rings;

import com.google.common.collect.Lists;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.Entity;
import net.minecraft.EntityXPOrb;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.StatCollector;
import net.minecraft.World;

import java.util.List;

public class MindStone extends RingToggle implements IPedestalItem
{
	private final int TRANSFER_RATE = 50;

	public MindStone() 
	{
		super("mind_stone");
		
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) 
	{
		if (world.isRemote || !(entity instanceof EntityPlayer))
		{
			return;
		}

		super.onUpdate(stack, world, entity, par4, par5);

		EntityPlayer player = (EntityPlayer) entity;

		if (getMode(stack) != 0) 
		{
			if (!canStore(stack))
			{
				this.changeMode(player, stack);
				return;
			}
			
			if (getXP(player) > 0)
			{
				int toAdd = getXP(player) >= TRANSFER_RATE ? TRANSFER_RATE : getXP(player);
				addStoredXP(stack, toAdd);
				removeXP(player, TRANSFER_RATE);
			}
		}
	}
	
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
	{
		if (!world.isRemote && getMode(stack) == 0 && getStoredXP(stack) != 0)
		{
			int toAdd = removeStoredXP(stack, TRANSFER_RATE);
			
			if (toAdd > 0)
			{
				addXP(player, toAdd);
				return true;
			}
		}
		
		return false;
	}
	
	private void removeXP(EntityPlayer player, int amount)
	{
		int experiencetotal = getXP(player) - amount;
		
		if (experiencetotal < 0)
		{
			player.experience = 0;
		}
		else
		{
			player.experience = experiencetotal;
		}
	}

	private void addXP(EntityPlayer player, int amount)
	{
		int experiencetotal = getXP(player) + amount;
		player.experience = experiencetotal;
	}

	private int getXP(EntityPlayer player)
	{
		return player.experience;
	}

	// Math referenced from the MC wiki
	private int getXPForLvl(int level) 
	{
		if (level < 0) 
		{
			return Integer.MAX_VALUE;
		}

		if (level <= 15) 
		{
			return level * 17;
		}

		if (level <= 30) 
		{
			return (int) (((level * level) * 1.5D) - (29.5D * level) + 360.0D);
		}

		return (int) (((level * level) * 3.5D) - (151.5D * level) + 2220.0D);
	}

	private int getLvlForXP(int totalXP)
	{
		int result = 0;

		while (getXPForLvl(result) <= totalXP) 
		{
			result++;
		}

		return --result;
	}
	
	private int getStoredXP(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new net.minecraft.NBTTagCompound();
		}
		return stack.stackTagCompound.getInteger("StoredXP");
	}
	
	private boolean canStore(ItemStack stack)
	{
		return getStoredXP(stack) <= Integer.MAX_VALUE;
	}

	private void setStoredXP(ItemStack stack, int XP) 
	{
		stack.stackTagCompound.setInteger("StoredXP", XP);
	}

	private void addStoredXP(ItemStack stack, int XP) 
	{
		long result = getStoredXP(stack) + XP;
		
		if (result > Integer.MAX_VALUE)
		{
			result = Integer.MAX_VALUE;
		}
		
		setStoredXP(stack, (int) result);
	}

	private int removeStoredXP(ItemStack stack, int XP) 
	{
		int currentXP = getStoredXP(stack);
		int result = 0;
		int returnResult = 0;
		
		if (currentXP < XP)
		{
			result = 0;
			returnResult = currentXP;
		}
		else
		{
			result = currentXP - XP;
			returnResult = XP;
		}
		
		setStoredXP(stack, result);
		return returnResult;
	}

	@Override
	public void updateInPedestal(World world, int x, int y, int z)
	{
		DMPedestalTile tile = ((DMPedestalTile) world.getBlockTileEntity(x, y, z));
		List<EntityXPOrb> orbs = world.getEntitiesWithinAABB(EntityXPOrb.class, tile.getEffectBounds());
		for (EntityXPOrb orb : orbs)
		{
			WorldHelper.gravitateEntityTowards(orb, x + 0.5, y + 0.5, z + 0.5);
			if (!world.isRemote && orb.getDistanceSq(x + 0.5,y + 0.5, z + 0.5) < 1.21)
			{
				suckXP(orb, tile.getItemStack());
			}
		}

	}

	private void suckXP(EntityXPOrb orb, ItemStack mindStone)
	{
		if (!mindStone.hasTagCompound())
		{
			mindStone.setTagCompound(new NBTTagCompound());
		}
		
		if (canStore(mindStone))
		{
			long l = getStoredXP(mindStone);
			if (l + orb.getXpValue() > Integer.MAX_VALUE)
			{
				orb.setDead();
				setStoredXP(mindStone, Integer.MAX_VALUE);
			}
			else
			{
				addStoredXP(mindStone, orb.getXpValue());
				orb.setDead();
			}
		}
	}

	@Override
	public List<String> getPedestalDescription()
	{
		return Lists.newArrayList(StatCollector.translateToLocal("pe.mind.pedestal1"));
	}

	@Override
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List list, boolean par4, net.minecraft.Slot slot)
	{
		super.addInformation(stack, player, list, par4, slot);
		list.add(net.minecraft.EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.mind.tooltip1"));
	}
}
