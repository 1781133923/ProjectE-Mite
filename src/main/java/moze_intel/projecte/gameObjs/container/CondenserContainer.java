package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.gameObjs.container.slots.condenser.SlotCondenserInput;
import moze_intel.projecte.gameObjs.container.slots.condenser.SlotCondenserLock;
import moze_intel.projecte.gameObjs.tiles.CondenserTile;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.EntityPlayer;
import net.minecraft.InventoryPlayer;
import net.minecraft.Container;
import net.minecraft.Slot;
import net.minecraft.ItemStack;

public class CondenserContainer extends Container
{	
	public CondenserTile tile;
	
	public CondenserContainer(InventoryPlayer invPlayer, CondenserTile condenser)
	{
		super(invPlayer.player);
		tile = condenser;
		tile.openChest();
		
		//Item Lock Slot
		this.addSlotToContainer(new SlotCondenserLock(this, 0, 12, 6));
		
		//Condenser Inventory
		for (int i = 0; i < 7; i++) 
			for (int j = 0; j < 13; j++)
				this.addSlotToContainer(new SlotCondenserInput(tile, 1 + j + i * 13, 12 + j * 18, 26 + i * 18));

		//Player Inventory
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++) 
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 48 + j * 18, 154 + i * 18));
		
		//Player Hotbar
		for (int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(invPlayer, i, 48 + i * 18, 212));
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
	{
		if (slotIndex == 0)
		{
			return null;
		}

		Slot slot = this.getSlot(slotIndex);
		
		if (slot == null || !slot.getHasStack())
		{
			return null;
		}
		
		ItemStack stack = slot.getStack();
		ItemStack newStack = stack.copy();

		if (slotIndex <= 91)
		{
			if (!this.mergeItemStack(stack, 92, 127, false))
			{
				return null;
			}
		}
		else if (!EMCHelper.doesItemHaveEmc(stack) || !this.mergeItemStack(stack, 1, 91, false))
		{
			return null;
		}
		
		if (stack.stackSize == 0)
		{
			slot.putStack(null);
		}
		
		else slot.onSlotChanged();
		slot.onPickupFromSlot(player, stack);
		return newStack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return player.getDistanceSq(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5) <= 64.0;
	}
	
	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed(player);
		tile.closeChest();
	}

	public ItemStack handleSlotClick(int slot, int button, int flag, boolean holding_shift, EntityPlayer player)
	{
		if (slot == 0 && tile.getStackInSlot(slot) != null)
		{
			if (!player.worldObj.isRemote)
			{
				tile.setInventorySlotContents(slot, null);
				tile.checkLockAndUpdate();
				this.detectAndSendChanges();
			}

			return null;
		}

		return super.slotClick(slot, button, flag, holding_shift, player);
	}
}
