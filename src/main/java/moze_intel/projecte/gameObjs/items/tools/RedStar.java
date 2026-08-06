package moze_intel.projecte.gameObjs.items.tools;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.BlockBreakInfo;
import net.minecraft.Block;
import net.minecraft.BlockDirt;
import net.minecraft.BlockGrass;
import net.minecraft.BlockGravel;
import net.minecraft.BlockClay;
import net.minecraft.BlockSand;
import net.minecraft.Material;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.MovingObjectPosition;

import net.minecraft.StatCollector;
import net.minecraft.World;

public class RedStar extends PEToolBase
{
	public RedStar() 
	{
		super("rm_morning_star", (byte) 4, new String[]{
				"pe.morningstar.mode1", "pe.morningstar.mode2",
				"pe.morningstar.mode3", "pe.morningstar.mode4",
				"pe.morningstar.mode_emc",
		});
		
		this.peToolMaterial = "rm_tools";
		this.pePrimaryToolClass = "morning_star";

		this.harvestMaterials.add(Material.grass);
		this.harvestMaterials.add(Material.dirt);
		this.harvestMaterials.add(Material.sand);
		this.harvestMaterials.add(Material.snow);
		this.harvestMaterials.add(Material.clay);
		
		this.harvestMaterials.add(Material.iron);
		this.harvestMaterials.add(Material.anvil);
		this.harvestMaterials.add(Material.stone);
		this.harvestMaterials.add(Material.obsidian);
		this.harvestMaterials.add(Material.netherrack);

		this.harvestMaterials.add(Material.wood);
		this.harvestMaterials.add(Material.plants);
		this.harvestMaterials.add(Material.vine);

		this.harvestBlocks.add(ObjHandler.matterBlock);
		this.harvestBlocks.add(ObjHandler.dmFurnaceOff);
		this.harvestBlocks.add(ObjHandler.dmFurnaceOn);
		this.harvestBlocks.add(ObjHandler.rmFurnaceOff);
		this.harvestBlocks.add(ObjHandler.rmFurnaceOn);

		this.secondaryClasses.add("pickaxe");
		this.secondaryClasses.add("chisel");
		this.secondaryClasses.add("shovel");
		this.secondaryClasses.add("axe");
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase damaged, EntityLivingBase damager)
	{
		// The morning star always deals its full listed damage (25).
		attackWithCharge(stack, damaged, damager, getAttackDamage());
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

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if (!world.isRemote)
		{
			if (this.isEmcMode(stack))
			{
				// EMC mode: dig a single flat layer and convert blocks to EMC.
				digAOE(stack, world, player, false, 0);
				return true;
			}

			if (ProjectEConfig.pickaxeAoeVeinMining)
			{
				mineOreVeinsInAOE(stack, player);
			}

			MovingObjectPosition mop = moze_intel.projecte.compat.PECompatHelper.getMovingObjectPositionFromPlayer(world, player);

			if (mop == null)
			{
				return true;
			}
			else if (mop.getEntityHit() == null)
			{
				Block block = world.getBlock(mop.blockX, mop.blockY, mop.blockZ);

				if (block instanceof BlockGravel || block instanceof BlockClay)
				{
					if (ProjectEConfig.pickaxeAoeVeinMining)
					{
						digAOE(stack, world, player, false, 0);
					}
					else
					{
						tryVeinMine(stack, player, mop);
					}
				}
				else if (ItemHelper.isOre(block, world.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ)))
				{
					if (!ProjectEConfig.pickaxeAoeVeinMining)
					{
						tryVeinMine(stack, player, mop);
					}
				}
				else if (block instanceof BlockGrass || block instanceof BlockDirt || block instanceof BlockSand)
				{
					digAOE(stack, world, player, false, 0);
				}
				else
				{
					digAOE(stack, world, player, false, 0);
				}
			}
		}
		
		return true;
	}
	
	public float getStrVsBlock(Block block, int metadata)
	{
		if (block == ObjHandler.matterBlock || block == ObjHandler.dmFurnaceOff || block == ObjHandler.dmFurnaceOn || block == ObjHandler.rmFurnaceOff || block == ObjHandler.rmFurnaceOn)
		{
			return MATTER_BLOCK_SPEED;
		}
		
		float base = super.getStrVsBlock(block, metadata);
		return base > 0.0F ? base + 48.0F : 0.0F;
	}

	@Override
	public boolean canBreakUnbreakable(Block block, int metadata)
	{
		// The morning star may break bedrock, but NOT the mantle or the core.
		return block == net.minecraft.Block.bedrock;
	}

	@Override
	public float getAttackDamage()
	{
		return 25.0F;
	}
	@Override
	public boolean isEmcMode(ItemStack stack)
	{
		return getMode(stack) == 4;
	}

	@Override
	public boolean hasAttributeDamage()
	{
		return !ProjectEConfig.useOldDamage;
	}
}
