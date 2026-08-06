package moze_intel.projecte.gameObjs.container.slots.collector;

import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.emc.FuelMapper;
import net.minecraft.IInventory;
import net.minecraft.Slot;
import net.minecraft.ItemStack;

public class SlotCollectorInv extends Slot 
{
	public SlotCollectorInv(IInventory inventory, int slotIndex, int xPos, int yPos) 
	{
		super(inventory, slotIndex, xPos, yPos);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		if (stack == null)
		{
			return false;
		}
		
		return stack.getItem() instanceof IItemEmc || (FuelMapper.isStackFuel(stack) && !FuelMapper.isStackMaxFuel(stack));
	}
}
