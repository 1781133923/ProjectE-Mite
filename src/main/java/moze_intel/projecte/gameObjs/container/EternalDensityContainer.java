package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.gameObjs.container.inventory.EternalDensityInventory;
import moze_intel.projecte.gameObjs.container.slots.SlotGhost;
import net.minecraft.EntityPlayer;
import net.minecraft.InventoryPlayer;
import net.minecraft.Container;
import net.minecraft.Slot;
import net.minecraft.ItemStack;

public class EternalDensityContainer extends Container
{
	private EternalDensityInventory inventory;
	
	public EternalDensityContainer(InventoryPlayer invPlayer, EternalDensityInventory gemInv)
	{
		super(invPlayer.player);
		inventory = gemInv;
		
		 for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 3; ++j)
			{
				this.addSlotToContainer(new SlotGhost(gemInv, j + i * 3, 62 + j * 18, 26 + i * 18));
			}

		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 93 + i * 18));
			}

		for (int i = 0; i < 9; ++i)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 151));
		}

	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
	{
		Slot slot = getSlot(slotIndex);
		if (slotIndex > 8)
		{
			int index = inventory.findFirstEmptySlot();
			if (index != -1)
			{
				ItemStack toSet = slot.getStack().copy();
				toSet.stackSize = 1;
				inventory.setInventorySlotContents(index, toSet);
			}
		}
		return null;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) 
	{
		return true;
	}
	
	public ItemStack handleSlotClick(int slot, int button, int flag, boolean holding_shift, EntityPlayer player)
	{
		if (slot >= 0 && getSlot(slot) != null && getSlot(slot).getStack() == player.getHeldItemStack()) 
		{
			return null;
		}
		
		if (slot >= 0 && slot < 9)
		{
			inventory.setInventorySlotContents(slot, null);
		}
		
		return super.slotClick(slot, button, flag, holding_shift, player);
	}
	
	@Override
	public boolean canDragIntoSlot(Slot slot) 
	{
		return false;
	}
}
