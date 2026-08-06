package moze_intel.projecte.gameObjs.items.itemBlocks;

import moze_intel.projecte.utils.AchievementHandler;
import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;
import net.minecraft.World;

public class ItemTransmutationBlock extends ItemBlock
{
	public ItemTransmutationBlock(Block block)
	{
		super(block);
	}
	
	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) 
	{
		if (world != null)
		{
			player.addStat(AchievementHandler.TRANSMUTATION, 1);
		}
	}
}
