package moze_intel.projecte.gameObjs.items.tools;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.Material;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.ItemStack;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import net.minecraftforge.common.IShearable;

public class DarkShears extends PEToolBase
{
	public DarkShears()
	{
		super("dm_shears", (byte)2, new String[]{});
		
		this.peToolMaterial = "dm_tools";
		this.pePrimaryToolClass = "shears";
		this.harvestMaterials.add(Material.web);
		this.harvestMaterials.add(Material.cloth);
		this.harvestMaterials.add(Material.plants);
		this.harvestMaterials.add(Material.tree_leaves);
		this.harvestMaterials.add(Material.vine);
	}

	// Only for RedShears
	protected DarkShears(String name, byte numCharges, String[] modeDesc)
	{
		super(name, numCharges, modeDesc);
	}

	public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase ent)
	{
		if (block.blockMaterial != Material.tree_leaves && block != Blocks.web && block != Blocks.tallgrass && block != Blocks.vine && block != Blocks.tripwire && !(block instanceof IShearable))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public boolean canHarvestBlock(Block block, ItemStack stack) 
	{
		return super.canHarvestBlock(block, stack) || block == Blocks.redstone_wire || block == Blocks.tripwire;
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down)
	{
		ItemStack stack = player.getHeldItemStack();

		// MITE-style: right-clicking a silk-harvestable block shears it directly.
		RaycastCollision rc = player.getSelectedObject(partial_tick, false);
		if (rc != null && rc.isBlock() && rc.canPlayerEditBlockHit(player, stack))
		{
			Block block = rc.getBlockHit();
			int metadata = rc.block_hit_metadata;
			if (block.canSilkHarvest(metadata) && isEffectiveAgainstBlock(block, metadata))
			{
				if (player.onClient())
				{
					player.swingArm();
				}
				else
				{
					BlockBreakInfo info = new BlockBreakInfo(player.worldObj, rc.block_hit_x, rc.block_hit_y, rc.block_hit_z)
							.setHarvestedBy(player);
					info.dropBlockAsItself(true);
					player.worldObj.playSoundAtBlock(rc.block_hit_x, rc.block_hit_y, rc.block_hit_z, "mob.sheep.shear", 1.0F, 1.0F);
				}
				return true;
			}
		}

		// ProjectE: AOE shear of all shearable entities around the player.
		shearEntityAOE(stack, player, 0);
		return true;
	}

	/**
	 * MITE-style shearing of sheep on right click (unbreakable, so the tool
	 * never takes damage from shearing).
	 */
	@Override
	public boolean tryEntityInteraction(Entity entity, EntityPlayer player, ItemStack stack)
	{
		if (entity instanceof EntitySheep)
		{
			EntitySheep sheep = (EntitySheep) entity;
			if (!sheep.getSheared() && !sheep.isChild())
			{
				if (!player.onServer())
				{
					return true;
				}
				shearSheep(sheep);
				return true;
			}
		}
		return false;
	}

	public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player)
	{
		shearBlock(stack, x, y, z, player);
		return false;
	}

	@Override
	public int getNumComponentsForDurability()
	{
		return 2;
	}

	@Override
	public float getAttackDamage()
	{
		return 5.0F;
	}
}
