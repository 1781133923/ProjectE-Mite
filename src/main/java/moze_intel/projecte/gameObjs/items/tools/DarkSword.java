package moze_intel.projecte.gameObjs.items.tools;

import moze_intel.projecte.api.item.IExtraFunction;
import moze_intel.projecte.config.ProjectEConfig;
import net.minecraft.Block;
import net.minecraft.Material;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.ItemStack;
import net.minecraft.World;

public class DarkSword extends PEToolBase implements IExtraFunction
{
	public DarkSword() 
	{
		super("dm_sword", (byte)2, new String[] {});
		
		this.peToolMaterial = "dm_tools";
		this.pePrimaryToolClass = "sword";
	}

	// Only for RedSword to use
	protected DarkSword(String name, byte numcharges, String[] modeDesc)
	{
		super(name, numcharges, modeDesc);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase damaged, EntityLivingBase damager)
	{
		boolean flag = ProjectEConfig.useOldDamage;
		attackWithCharge(stack, damaged, damager, flag ? DARKSWORD_BASE_ATTACK : 1.0F);
		return true;
	}

	public float getStrVsBlock(Block block, int meta)
	{
		if (block == Blocks.web)
		{
			return 15.0F;
		}
		else
		{
			Material material = block.blockMaterial;
			return material != Material.plants && material != Material.vine && material != Material.coral && material != Material.tree_leaves && material != Material.pumpkin ? 0.0F : 1.5F;
		}
	}

	@Override
	public EnumItemInUseAction getItemInUseAction(ItemStack stack, EntityPlayer player)
	{
		return EnumItemInUseAction.BLOCK;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down)
	{
		player.setHeldItemInUse();
		return true;
	}

	public boolean canHarvestBlock(Block p_150897_1_, ItemStack stack)
	{
		return p_150897_1_ == net.minecraft.Block.web;
	}

	@Override
	public void doExtraFunction(ItemStack stack, EntityPlayer player)
	{
		// C-key special attack deals a flat 20 damage in an AOE.
		attackAOE(stack, player, false, 20.0F, 0);
	}

	@Override
	public float getAttackDamage()
	{
		return this instanceof RedSword ? REDSWORD_BASE_ATTACK : DARKSWORD_BASE_ATTACK;
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
