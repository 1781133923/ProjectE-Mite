package moze_intel.projecte.gameObjs.container.slots.relay;

import moze_intel.projecte.api.item.IItemEmc;
import net.minecraft.IInventory;
import net.minecraft.Slot;
import net.minecraft.ItemStack;

public class SlotRelayKlein extends Slot
{
	public SlotRelayKlein(IInventory inventory, int slotIndex, int xPos, int yPos) 
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
		
		return stack.getItem() instanceof IItemEmc;
	}
}
