package moze_intel.projecte.gameObjs.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.item.IAlchBagItem;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.container.AlchBagContainer;
import moze_intel.projecte.playerData.AlchemicalBags;
import moze_intel.projecte.utils.AchievementHandler;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.IconRegister;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Icon;
import net.minecraft.MathHelper;
import net.minecraft.StatCollector;
import net.minecraft.World;

import java.util.List;

public class AlchemicalBag extends ItemPE
{
	private final String[] colors = new String[] {"white", "orange", "magenta", "lightBlue", "yellow", "lime", "pink", "gray", "silver", "cyan", "purple", "blue", "brown", "green", "red", "black"};

	// MC Lang files have these unlocalized names mapped to raw color names
	private final String[] unlocalizedColors = new String[] {
			"item.fireworksCharge.white", "item.fireworksCharge.orange",
			"item.fireworksCharge.magenta", "item.fireworksCharge.lightBlue",
			"item.fireworksCharge.yellow", "item.fireworksCharge.lime",
			"item.fireworksCharge.pink", "item.fireworksCharge.gray",
			"item.fireworksCharge.silver", "item.fireworksCharge.cyan",
			"item.fireworksCharge.purple", "item.fireworksCharge.blue",
			"item.fireworksCharge.brown", "item.fireworksCharge.green",
			"item.fireworksCharge.red", "item.fireworksCharge.black"};
	
	@SideOnly(Side.CLIENT)
	private Icon[] icons;
	
	public AlchemicalBag()
	{
		this.setUnlocalizedName("alchemical_bag");
		this.setSubtypes(16);
	
		this.setMaxStackSize(1);
			}
	
	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if (!world.isRemote)
		{
			moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.ALCH_BAG_GUI, world, (int) player.posX, (int) player.posY, (int) player.posZ);
		}
		
		return true;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) 
	{
		if (!(entity instanceof EntityPlayer))
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer) entity;
		ItemStack[] inv = AlchemicalBags.get(player, (byte) stack.getItemSubtype());

		if (player.openContainer instanceof AlchBagContainer)
		{
			ItemStack[] openContainerInv = ((AlchBagContainer) player.openContainer).inventory.getInventory();
			for (int i = 0; i < openContainerInv.length; i++) // Do not use foreach - to avoid desync
			{
				ItemStack current = openContainerInv[i];
				if (current != null && current.getItem() instanceof IAlchBagItem)
				{
					((IAlchBagItem) current.getItem()).updateInAlchBag(openContainerInv, player, current);
				}
			}
			// Do not AlchemicalBags.set/syncPartial here - vanilla handles it because it's the open container
		}
		else
		{
			boolean hasChanged = false;
			for (int i = 0; i < inv.length; i++) // Do not use foreach - to avoid desync
			{
				ItemStack current = inv[i];
				if (current != null && current.getItem() instanceof IAlchBagItem)
				{
					hasChanged = ((IAlchBagItem) current.getItem()).updateInAlchBag(inv, player, current);
				}
			}

			if (!player.worldObj.isRemote && hasChanged)
			{
				AlchemicalBags.set(player, ((byte) stack.getItemSubtype()), inv);
				AlchemicalBags.syncPartial(player, stack.getItemSubtype());
			}
		}
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) 
	{
		return 1; 
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		String name = super.getItemStackDisplayName(stack);
		int i = stack.getItemSubtype();

		if (stack.getItemSubtype() > 15)
		{
			return name + " (" + StatCollector.translateToLocal("pe.debug.metainvalid.name") + ")";
		}

		String color = " (" + StatCollector.translateToLocal(unlocalizedColors[i]) + ")";
		return name + color;
	}
	
	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) 
	{
		super.onCreated(stack, world, player);
		
		if (!world.isRemote)
		{
			player.addStat(AchievementHandler.ALCH_BAG, 1);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(int itemID, CreativeTabs cTab, List list)
	{
		for (int i = 0; i < 16; ++i)
			list.add(new ItemStack(this, 1, i));
	}
	
	@SideOnly(Side.CLIENT)
	public Icon getIconFromSubtype(int par1)
	{
		return icons[MathHelper.clamp_int(par1, 0, 15)];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		icons = new Icon[16];
		
		for (int i = 0; i < 16; i++)
		{
			icons[i] = register.registerIcon(this.getTexture("alchemy_bags", colors[i]));
		}
	}

	public static ItemStack getFirstBagWithSuctionItem(EntityPlayer player, ItemStack[] inventory)
	{
		for (ItemStack stack : inventory)
		{
			if (stack == null)
			{
				continue;
			}

			if (stack.getItem() == ObjHandler.alchBag)
			{
				ItemStack[] inv = AlchemicalBags.get(player, ((byte) stack.getItemSubtype()));
				if (ItemHelper.invContainsItem(inv, new ItemStack(ObjHandler.blackHole, 1, 1))
						|| ItemHelper.invContainsItem(inv, new ItemStack(ObjHandler.voidRing, 1, 1)))
				return stack;
			}
		}

		return null;
	}
}
