package moze_intel.projecte.gameObjs.items.tools;

import moze_intel.projecte.api.item.IExtraFunction;
import moze_intel.projecte.config.ProjectEConfig;
import net.minecraft.*;
import net.minecraft.Material;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.StatCollector;
import net.minecraft.World;

public class RedKatar extends PEToolBase implements IExtraFunction
{
	public RedKatar() 
	{
		super("rm_katar", (byte)4, new String[] {
				"pe.katar.mode1", "pe.katar.mode2",
		});
		
		this.peToolMaterial = "rm_tools";
		this.pePrimaryToolClass = "katar";
		this.harvestMaterials.add(Material.wood);
		this.harvestMaterials.add(Material.web);
		this.harvestMaterials.add(Material.cloth);
		this.harvestMaterials.add(Material.plants);
		this.harvestMaterials.add(Material.tree_leaves);
		this.harvestMaterials.add(Material.vine);

		this.secondaryClasses.add("sword");
		this.secondaryClasses.add("axe");
		this.secondaryClasses.add("shears");
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase damaged, EntityLivingBase damager)
	{
		boolean flag = ProjectEConfig.useOldDamage;
		attackWithCharge(stack, damaged, damager, flag ? KATAR_BASE_ATTACK : 1.0F);
		return true;
	}

	public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player)
	{
		// Shear
		shearBlock(stack, x, y, z, player);
		return false;
	}
	
	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		
		if (world.isRemote)
		{
			return true;
		}

		// Right-click the ground: AOE till (same as the dark/red matter hoes).
		RaycastCollision rc = player.getSelectedObject(partial_tick, true);
		if (rc != null && rc.isBlock() && rc.face_hit != null && rc.face_hit.isTop())
		{
			tillAOE(stack, player, rc.world, rc.block_hit_x, rc.block_hit_y, rc.block_hit_z, 0, 0);
			return true;
		}

		byte charge = getCharge(stack);

		// AOE shear: MITE sheep around the player
		shearSheepAOE(stack, player);
		// AOE shear: IShearable entities around the player
		shearEntityAOE(stack, player, 0);
		// AOE shear leaves around the player (radius grows with charge)
		clearOdAOERadius(world, stack, player, "treeLeaves", 0, 2 + charge, 2 + charge);
		
		return true;
	}

	@Override
	public void doExtraFunction(ItemStack stack, EntityPlayer player)
	{
		attackAOE(stack, player, getMode(stack) == 1, ProjectEConfig.katarDeathAura, 0);
	}

	@Override
	public float getAttackDamage()
	{
		return KATAR_BASE_ATTACK;
	}

	@Override
	public boolean hasAttributeDamage()
	{
		return !ProjectEConfig.useOldDamage;
	}

	@Override
	public int getNumComponentsForDurability()
	{
		return 2;
	}

}
