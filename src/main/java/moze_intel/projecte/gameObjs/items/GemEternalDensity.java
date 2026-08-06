package moze_intel.projecte.gameObjs.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.google.common.collect.Lists;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.item.IAlchBagItem;
import moze_intel.projecte.api.item.IAlchChestItem;
import moze_intel.projecte.api.item.IModeChanger;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.tiles.AlchChestTile;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.PELogger;
import moze_intel.projecte.utils.WorldHelper;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.IconRegister;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.NBTTagList;
import net.minecraft.ChatComponentTranslation;
import net.minecraft.Icon;
import net.minecraft.StatCollector;
import net.minecraft.World;
import net.minecraftforge.common.util.Constants.NBT;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class GemEternalDensity extends ItemPE implements IAlchBagItem, IAlchChestItem, IModeChanger, IBauble
{
	@SideOnly(Side.CLIENT)
	private Icon gemOff;
	@SideOnly(Side.CLIENT)
	private Icon gemOn;
	
	public GemEternalDensity()
	{
		this.setUnlocalizedName("gem_density");
		this.setMaxStackSize(1);
	}

	private static byte getGemMode(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new NBTTagCompound();
		}
		return stack.stackTagCompound.getByte("On");
	}

	private static void setGemMode(ItemStack stack, int mode)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new NBTTagCompound();
		}
		stack.stackTagCompound.setByte("On", (byte) mode);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) 
	{
		if (!stack.hasTagCompound())
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		if (world.isRemote || slot > 8 || !(entity instanceof EntityPlayer))
		{
			return;
		}
		
		condense(stack, ((EntityPlayer) entity).inventory.mainInventory);
	}

	/**
	 * @return Whether the inventory was changed
	 */
	public static boolean condense(ItemStack gem, ItemStack[] inv)
	{
		if (getGemMode(gem) == 0 || ItemPE.getEmc(gem) >= Constants.TILE_MAX_EMC)
		{
			return false;
		}

		boolean hasChanged = false;
		boolean isWhitelist = isWhitelistMode(gem);
		List<ItemStack> whitelist = getWhitelist(gem);
		
		ItemStack target = getTarget(gem);
		
		for (int i = 0; i < inv.length; i++)
		{
			ItemStack s = inv[i];
			
			if (s == null || !EMCHelper.doesItemHaveEmc(s) || s.getMaxStackSize() == 1 || EMCHelper.getEmcValue(s) >= EMCHelper.getEmcValue(target))
			{
				continue;
			}
			
			if ((isWhitelist && listContains(whitelist, s)) || (!isWhitelist && !listContains(whitelist, s)))
			{
				ItemStack copy = s.copy();
				copy.stackSize = s.stackSize == 1 ? 1 : s.stackSize / 2;

				addToList(gem, copy);
				
				s.stackSize -= copy.stackSize;
				
				if (s.stackSize <= 0)
				{
					inv[i] = null;
				}
				
				ItemPE.addEmcToStack(gem, EMCHelper.getEmcValue(copy) * copy.stackSize);
				hasChanged = true;
				break;
			}
		}
		
		int value = EMCHelper.getEmcValue(target);

		if (!EMCHelper.doesItemHaveEmc(target))
		{
			return hasChanged;
		}

		while (getEmc(gem) >= value)
		{
			ItemStack remain = ItemHelper.pushStackInInv(inv, ItemStack.copyItemStack(target));

			if (remain != null)
			{
				return false;
			}
			
			ItemPE.removeEmc(gem, value);
			setItems(gem, Lists.<ItemStack>newArrayList());
			hasChanged = true;
		}

		return hasChanged;
	}
	
	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if (!world.isRemote)
		{
			if (player.isSneaking())
			{
				if (getGemMode(stack) == 1)
				{
					List<ItemStack> items = getItems(stack);
					
					if (!items.isEmpty())
					{
						WorldHelper.createLootDrop(items, world, player.posX, player.posY, player.posZ);

						setItems(stack, new ArrayList<ItemStack>());
						ItemPE.setEmc(stack, 0);
					}
					
					setGemMode(stack, 0);
				}
				else
				{
					setGemMode(stack, 1);
				}
			}
			else
			{
				moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.ETERNAL_DENSITY_GUI, world, (int) player.posX, (int) player.posY, (int) player.posZ);
			}
		}
		
		return true;
	}
	
	private String getTargetName(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new net.minecraft.NBTTagCompound();
		}
		switch(stack.stackTagCompound.getByte("Target"))
		{
			case 0:
				return "item.ingotIron.name";
			case 1:
				return "item.ingotGold.name";
			case 2:
				return "item.diamond.name";
			case 3:
				return "item.pe_matter_dark.name";
			case 4:
				return "item.pe_matter_red.name";
			default:
				return "INVALID";
		}
	}
	
	private static ItemStack getTarget(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new net.minecraft.NBTTagCompound();
		}
		switch (stack.stackTagCompound.getByte("Target"))
		{
			case 0:
				return new ItemStack(Items.iron_ingot);
			case 1:
				return new ItemStack(Items.gold_ingot);
			case 2:
				return new ItemStack(Items.diamond);
			case 3:
				return new ItemStack(ObjHandler.matter, 1, 0);
			case 4:
				return new ItemStack(ObjHandler.matter, 1, 1);
			default:
				PELogger.logFatal("Invalid target for gem of eternal density: " + stack.stackTagCompound.getByte("Target"));
				return null;
		}
	}
	
	private static void setItems(ItemStack stack, List<ItemStack> list)
	{
		NBTTagList tList = new NBTTagList();
		
		for (ItemStack s : list)
		{
			NBTTagCompound nbt = new NBTTagCompound();
			s.writeToNBT(nbt);
			tList.appendTag(nbt);
		}
		
		stack.stackTagCompound.setTag("Consumed", tList);
	}
	
	private static List<ItemStack> getItems(ItemStack stack)
	{
		List<ItemStack> list = Lists.newArrayList();
		NBTTagList tList = stack.stackTagCompound.getTagList("Consumed");
		
		for (int i = 0; i < tList.tagCount(); i++)
		{
			list.add(ItemStack.loadItemStackFromNBT(((NBTTagCompound) tList.tagAt(i))));
		}
		
		return list;
	}
	
	private static void addToList(ItemStack gem, ItemStack stack)
	{
		List<ItemStack> list = getItems(gem);
		
		addToList(list, stack);
		
		setItems(gem, list);
	}
	
	private static void addToList(List<ItemStack> list, ItemStack stack)
	{
		boolean hasFound = false;

		for (ItemStack s : list)
		{
			if (s.stackSize < s.getMaxStackSize() && ItemHelper.areItemStacksEqual(s, stack))
			{
				int remain = s.getMaxStackSize() - s.stackSize;

				if (stack.stackSize <= remain)
				{
					s.stackSize += stack.stackSize;
					hasFound = true;
					break;
				}
				else
				{
					s.stackSize += remain;
					stack.stackSize -= remain;
				}
			}
		}

		if (!hasFound)
		{
			list.add(stack);
		}
	}
	
	private static boolean isWhitelistMode(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new net.minecraft.NBTTagCompound();
		}
		return stack.stackTagCompound.getBoolean("Whitelist");
	}
	
	private static List<ItemStack> getWhitelist(ItemStack stack)
	{
		List<ItemStack> result = Lists.newArrayList();
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new net.minecraft.NBTTagCompound();
		}
		NBTTagList list = stack.stackTagCompound.getTagList("Items");
		
		for (int i = 0; i < list.tagCount(); i++)
		{
			result.add(ItemStack.loadItemStackFromNBT(((NBTTagCompound) list.tagAt(i))));
		}
		
		return result;
	}
	
	private static boolean listContains(List<ItemStack> list, ItemStack stack)
	{
		for (ItemStack s : list)
		{
			if (ItemHelper.areItemStacksEqual(s, stack))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public byte getMode(ItemStack stack)
	{
		if (stack.hasTagCompound())
		{
			return stack.stackTagCompound.getByte("Target");
		}

		return 0;
	}

	@Override
	public void changeMode(EntityPlayer player, ItemStack stack)
	{
		byte oldMode = getMode(stack);

		if (oldMode == 4)
		{
			stack.stackTagCompound.setByte("Target", (byte) 0);
		}
		else
		{
			stack.stackTagCompound.setByte("Target", (byte) (oldMode + 1));
		}

		moze_intel.projecte.compat.PEChatHelper.send(player, new ChatComponentTranslation("pe.gemdensity.mode_switch").appendText(" ").appendSibling(new ChatComponentTranslation(getTargetName(stack))));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4, net.minecraft.Slot slot) 
	{
		list.add(StatCollector.translateToLocal("pe.gemdensity.tooltip1"));
		
		if (stack.hasTagCompound())
		{
			list.add(String.format(StatCollector.translateToLocal("pe.gemdensity.tooltip2"), StatCollector.translateToLocal(getTargetName(stack))));
		}
		list.add(String.format(StatCollector.translateToLocal("pe.gemdensity.tooltip3"), ClientKeyHelper.getKeyName(PEKeybind.MODE)));
		list.add(StatCollector.translateToLocal("pe.gemdensity.tooltip4"));
		list.add(StatCollector.translateToLocal("pe.gemdensity.tooltip5"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromSubtype(int dmg)
	{
		return dmg == 0 ? gemOff : gemOn;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		gemOn = register.registerIcon(this.getTexture("dense_gem_on"));
		gemOff = register.registerIcon(this.getTexture("dense_gem_off"));
	}
	
	@Override
	@Optional.Method(modid = "Baubles")
	public baubles.api.BaubleType getBaubleType(ItemStack itemstack)
	{
		return BaubleType.RING;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onWornTick(ItemStack stack, EntityLivingBase player) 
	{
		this.onUpdate(stack, player.worldObj, player, 0, false);
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onEquipped(ItemStack itemstack, EntityLivingBase player) {}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {}

	@Override
	@Optional.Method(modid = "Baubles")
	public boolean canEquip(ItemStack itemstack, EntityLivingBase player) 
	{
		return true;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) 
	{
		return true;
	}

	@Override
	public void updateInAlchChest(World world, int x, int y, int z, ItemStack stack)
	{
		if (!world.isRemote && getGemMode(stack) == 1)
		{
			AlchChestTile tile = ((AlchChestTile) world.getBlockTileEntity(x, y, z));
			condense(stack, tile.getBackingInventoryArray());
			tile.onInventoryChanged();
		}
	}

	@Override
	public boolean updateInAlchBag(ItemStack[] inv, EntityPlayer player, ItemStack stack)
	{
		return !player.worldObj.isRemote && condense(stack, inv);
	}
}
