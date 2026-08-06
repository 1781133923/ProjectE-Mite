package moze_intel.projecte.gameObjs.container.slots;

import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.Container;
import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.Slot;
import net.minecraft.ItemStack;

public class SlotGhost extends Slot
{
	public SlotGhost(IInventory inv, int slotIndex, int xPos, int yPost) 
	{
		super(inv, slotIndex, xPos, yPost);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		if (stack != null && EMCHelper.doesItemHaveEmc(stack))
		{
			this.putStack(ItemHelper.getNormalizedStack(stack));
		}
		
		return false;
	}

	@Override
	public boolean canTakeStack(EntityPlayer player)
	{
		return false;
	}

	@Override
	public void onSlotClicked(EntityPlayer entity_player, int button, Container container)
	{
		// MITE 的 Container.slotClick 是 final 的，原 ProjectE 的
		// handleSlotClick（点击槽位从白/黑名单移除标记）永远不会被调用，
		// 所以这里借助 MITE 的 Slot.onSlotClicked 钩子实现：
		// 点击已标记的物品槽位 = 把该标记从名单中移除。
		if (this.getHasStack())
		{
			this.putStack(null);
		}
	}
	
	@Override
	public int getSlotStackLimit() 
	{
		return 1;
	}
}
