package moze_intel.projecte.gameObjs.container.inventory;

import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.UpdateGemModePKT;
import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class EternalDensityInventory implements IInventory
{

	@Override
	public void destroyInventory() {}
	private ItemStack inventory[];
	private EntityPlayer player;
	private boolean isInWhitelist;
	
	public EternalDensityInventory(ItemStack stack, EntityPlayer player) 
	{
		inventory = new ItemStack[9];
		
		if (!stack.hasTagCompound())
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		readFromNBT(stack.stackTagCompound);
		
		this.player = player;
	}
	

	@Override
	public int getSizeInventory()
	{
		return 9;
	}

	@Override
	public ItemStack getStackInSlot(int slot) 
	{
		return inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int num) 
	{
		ItemStack stack = getStackInSlot(slot);
		
		if(stack != null)
		{
			if(stack.stackSize > num)
			{
				stack = stack.splitStack(num);
				onInventoryChanged();
			}
			else
			{
				setInventorySlotContents(slot, null);
			}
		}
		
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) 
	{
		ItemStack stack = getStackInSlot(slot);
		
		if(stack != null)
		{
			setInventorySlotContents(slot, null);
		}
		
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) 
	{
		this.inventory[slot] = stack;

		if (stack != null && stack.stackSize > this.getInventoryStackLimit())
		{
			stack.stackSize = this.getInventoryStackLimit();
		}
		
		onInventoryChanged();
	}

	@Override
	public String getCustomNameOrUnlocalized() {
		return null;
	}

	@Override
	public boolean hasCustomName() 
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return 64;
	}

	@Override
	public void onInventoryChanged() 
	{
		for (int i = 0; i < inventory.length; ++i)
		{
			if (inventory[i] != null && inventory[i].stackSize == 0)
			{
				inventory[i] = null;
			}
		}
		
		if (player.getHeldItemStack() != null)
		{
			writeToNBT(player.getHeldItemStack().stackTagCompound);
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) 
	{
		return true;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) 
	{
		return true;
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		isInWhitelist = nbt.getBoolean("Whitelist");
		
		NBTTagList items = nbt.getTagList("Items");
		
		for (int i = 0; i < items.tagCount(); i++)
		{
			NBTTagCompound tag = ((NBTTagCompound) items.tagAt(i));
			
			inventory[tag.getByte("Slot")] = ItemStack.loadItemStackFromNBT(tag);
		}
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setBoolean("Whitelist", isInWhitelist);
		
		NBTTagList items = new NBTTagList();
		
		for (int i = 0; i < inventory.length; i++)
		{
			if (inventory[i] != null)
			{
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				inventory[i].writeToNBT(tag);
				items.appendTag(tag);
			}
		}
		
		nbt.setTag("Items", items);
	}
	
	public void changeMode()
	{
		isInWhitelist = !isInWhitelist;
		onInventoryChanged();
		
		PacketHandler.sendToServer(new UpdateGemModePKT(isInWhitelist));
	}
	
	public boolean isWhitelistMode()
	{
		return isInWhitelist;
	}

	public int findFirstEmptySlot()
	{
		for (int i = 0; i < inventory.length; i++)
		{
			if (inventory[i] == null)
			{
				return i;
			}
		}
		return -1;
	}
}
