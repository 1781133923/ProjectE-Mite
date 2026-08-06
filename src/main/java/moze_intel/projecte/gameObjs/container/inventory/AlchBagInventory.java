package moze_intel.projecte.gameObjs.container.inventory;

import moze_intel.projecte.playerData.AlchemicalBags;
import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.ItemStack;

public class AlchBagInventory implements IInventory
{

	@Override
	public void destroyInventory() {}
	private final ItemStack invItem;
	private ItemStack[] inventory;
	private EntityPlayer player;
	
	public AlchBagInventory(EntityPlayer player, ItemStack stack)
	{
		invItem = stack;
		this.player = player;
		inventory = AlchemicalBags.get(player, (byte) stack.getItemSubtype());
	}

	@Override
	public int getSizeInventory() 
	{
		return 104;
	}

	@Override
	public ItemStack getStackInSlot(int slot) 
	{
		return inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int qty)
	{
		ItemStack stack = getStackInSlot(slot);
		
		if (stack != null)
		{
			if(stack.stackSize > qty)
			{
				stack = stack.splitStack(qty);
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
		
		if (stack != null)
		{
			setInventorySlotContents(slot, null);
		}
		
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) 
	{
		inventory[slot] = stack;
		
		if (stack != null && stack.stackSize > getInventoryStackLimit())
		{
			stack.stackSize = this.getInventoryStackLimit();
		}
		
		onInventoryChanged();
	}

	@Override
	public String getCustomNameOrUnlocalized() {
		return "item.pe_alchemical_bag_white.name";
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
		for (int i = 0; i < 104; ++i)
		{
			if (getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0)
			{
				inventory[i] = null;
			}
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) 
	{
		return true;
	}

	@Override
	public void openChest() 
	{
	}

	@Override
	public void closeChest() 
	{
		if (!player.worldObj.isRemote)
		{
			AlchemicalBags.set(player, (byte) invItem.getItemSubtype(), inventory);
			AlchemicalBags.syncPartial(player, invItem.getItemSubtype());
		}
	}
	
	public ItemStack[] getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) 
	{
		return true;
	}
}
