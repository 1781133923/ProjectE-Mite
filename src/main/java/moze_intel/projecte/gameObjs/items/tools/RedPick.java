package moze_intel.projecte.gameObjs.items.tools;

import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.utils.AchievementHandler;
import net.minecraft.Block;
import net.minecraft.Material;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.StatCollector;
import net.minecraft.World;

public class RedPick extends DarkPick
{
	public RedPick()
	{
		super("rm_pick", (byte)3, new String[] {
				"pe.redpick.mode1", "pe.redpick.mode2",
				"pe.redpick.mode3", "pe.redpick.mode4"});
		
		this.peToolMaterial = "rm_tools";
		this.pePrimaryToolClass = "pickaxe";
		this.harvestMaterials.add(Material.iron);
		this.harvestMaterials.add(Material.anvil);
		this.harvestMaterials.add(Material.stone);
		this.harvestMaterials.add(Material.obsidian);
		this.harvestMaterials.add(Material.netherrack);
		this.harvestBlocks.add(ObjHandler.matterBlock);
		this.harvestBlocks.add(ObjHandler.rmFurnaceOff);
		this.harvestBlocks.add(ObjHandler.rmFurnaceOn);
	}
	
	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) 
	{
		super.onCreated(stack, world, player);
		
		if (!world.isRemote)
		{
			player.addStat(AchievementHandler.RM_PICK, 1);
		}
	}

	public float getStrVsBlock(Block block, int metadata)
	{
		if ((block == ObjHandler.matterBlock && metadata == 1) || block == ObjHandler.rmFurnaceOff || block == ObjHandler.rmFurnaceOn)
		{
			return MATTER_BLOCK_SPEED;
		}
		
		return super.getStrVsBlock(block, metadata);
	}

	@Override
	public float getAttackDamage()
	{
		return 35.0F;
	}
}
