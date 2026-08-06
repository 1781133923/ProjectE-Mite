package moze_intel.projecte.gameObjs.container.slots.mercurial;

import moze_intel.projecte.gameObjs.items.KleinStar;
import net.minecraft.IInventory;
import net.minecraft.Slot;
import net.minecraft.ItemStack;

public class SlotMercurialKlein extends Slot
{
	public SlotMercurialKlein(IInventory par1iInventory, int par2, int par3, int par4) 
	{
		super(par1iInventory, par2, par3, par4);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return stack.getItem() instanceof KleinStar;
	}
}
