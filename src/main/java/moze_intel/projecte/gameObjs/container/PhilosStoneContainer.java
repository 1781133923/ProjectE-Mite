package moze_intel.projecte.gameObjs.container;

import net.minecraft.EntityPlayer;
import net.minecraft.InventoryPlayer;
import net.minecraft.MITEContainerCrafting;
import net.minecraft.Slot;
import net.minecraft.SlotCrafting;
import net.minecraft.ItemStack;

/**
 * Philosopher's stone 3x3 crafting table. Extends MITE's crafting container
 * (not the vanilla Container) so the crafting GUI tooltip logic - which casts
 * any container with a SlotCrafting to MITEContainerCrafting - does not crash.
 * The stone's table is treated as an adamantium-tier workbench (see
 * ProjectEPhiloBenchMixin), so every ProjectE/MITE recipe can be crafted there
 * at adamantium crafting speed.
 */
public class PhilosStoneContainer extends MITEContainerCrafting
{
	public PhilosStoneContainer(InventoryPlayer invPlayer)
	{
		super(invPlayer.player);
		// MITEContainerCrafting.<init> already calls getMatrixSize(),
		// createSlots() and onCraftMatrixChanged().
	}

	@Override
	public int getMatrixSize()
	{
		return 3;
	}

	@Override
	public void createSlots()
	{
		// Crafting result
		this.addSlotToContainer(new SlotCrafting(this.player, this.craft_matrix, this.craft_result, 0, 124, 35));

		// 3x3 crafting grid
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				this.addSlotToContainer(new Slot(this.craft_matrix, j + i * 3, 30 + j * 18, 17 + i * 18));

		// Player inventory
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				this.addSlotToContainer(new Slot(this.player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

		// Player hotbar
		for (int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(this.player.inventory, i, 8 + i * 18, 142));
	}

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed(player);

		if (!this.player.worldObj.isRemote)
		{
			for (int i = 0; i < 9; ++i)
			{
				ItemStack itemstack = this.craft_matrix.getStackInSlotOnClosing(i);
				if (itemstack != null)
				{
					player.dropPlayerItemWithRandomChoice(itemstack, false);
				}
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index == 0)
			{
				if (!this.mergeItemStack(itemstack1, 10, 46, true))
				{
					return null;
				}

				slot.onSlotChange(itemstack1, itemstack);
			}
			else if (index >= 10 && index < 37)
			{
				if (!this.mergeItemStack(itemstack1, 37, 46, false))
				{
					return null;
				}
			}
			else if (index >= 37 && index < 46)
			{
				if (!this.mergeItemStack(itemstack1, 10, 37, false))
				{
					return null;
				}
			}
			else if (!this.mergeItemStack(itemstack1, 10, 46, false))
			{
				return null;
			}

			if (itemstack1.stackSize == 0)
			{
				slot.putStack((ItemStack) null);
			}
			else
			{
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize)
			{
				return null;
			}

			slot.onPickupFromSlot(player, itemstack1);
		}

		return itemstack;
	}

	@Override
	public boolean func_94530_a(ItemStack stack, Slot slot)
	{
		return slot.inventory != this.craft_result && super.func_94530_a(stack, slot);
	}
}
