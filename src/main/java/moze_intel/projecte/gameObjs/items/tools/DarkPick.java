package moze_intel.projecte.gameObjs.items.tools;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.utils.AchievementHandler;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.BlockBreakInfo;
import net.minecraft.Block;
import net.minecraft.Material;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.MovingObjectPosition;
import net.minecraft.StatCollector;
import net.minecraft.World;

public class DarkPick extends PEToolBase
{
	public DarkPick()
	{
		super("dm_pick", (byte)2, new String[] {
				"pe.darkpick.mode1", "pe.darkpick.mode2",
				"pe.darkpick.mode3", "pe.darkpick.mode4"});
		
		this.peToolMaterial = "dm_tools";
		this.pePrimaryToolClass = "pickaxe";
		this.harvestMaterials.add(Material.iron);
		this.harvestMaterials.add(Material.anvil);
		this.harvestMaterials.add(Material.stone);
		this.harvestMaterials.add(Material.obsidian);
		this.harvestMaterials.add(Material.netherrack);
		this.harvestBlocks.add(ObjHandler.matterBlock);
		this.harvestBlocks.add(ObjHandler.dmFurnaceOff);
		this.harvestBlocks.add(ObjHandler.dmFurnaceOn);
		this.harvestBlocks.add(ObjHandler.transmuteStone);
	}

	// Only for RedPick
	protected DarkPick(String name, byte numCharges, String[] modeDesc)
	{
		super(name, numCharges, modeDesc);
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if (world.isRemote)
		{
			return true;
		}

		if (ProjectEConfig.pickaxeAoeVeinMining)
		{
			mineOreVeinsInAOE(stack, player);
		}
		else
		{
			MovingObjectPosition mop = moze_intel.projecte.compat.PECompatHelper.getMovingObjectPositionFromPlayer(world, player);
			if (mop != null && mop.getEntityHit() == null)
			{
				Block b = world.getBlock(mop.blockX, mop.blockY, mop.blockZ);
				if (ItemHelper.isOre(b, world.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ)))
				{
					tryVeinMine(stack, player, mop);
				}
			}
		}

		return true;
	}

	@Override
	protected void onToolBlockDestroyed(BlockBreakInfo info)
	{
		ItemStack stack = info.getHarvesterItemStack();
		if (stack != null && info.getHarvester() instanceof EntityPlayer)
		{
			digBasedOnMode(stack, info.world, info.block, info.x, info.y, info.z, info.getHarvester());
		}
	}

	public float getStrVsBlock(Block block, int metadata)
	{
		if ((block == ObjHandler.matterBlock && metadata == 0) || block == ObjHandler.dmFurnaceOff || block == ObjHandler.dmFurnaceOn)
		{
			return MATTER_BLOCK_SPEED;
		}
		
		return super.getStrVsBlock(block, metadata);
	}
	
	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) 
	{
		super.onCreated(stack, world, player);
		
		if (!world.isRemote)
		{
			player.addStat(AchievementHandler.DM_PICK, 1);
		}
	}

	@Override
	public float getAttackDamage()
	{
		return 20.0F;
	}
}
