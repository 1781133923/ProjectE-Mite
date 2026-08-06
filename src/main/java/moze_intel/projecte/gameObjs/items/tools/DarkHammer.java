package moze_intel.projecte.gameObjs.items.tools;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.BlockBreakInfo;
import net.minecraft.Block;
import net.minecraft.Material;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.World;

public class DarkHammer extends PEToolBase
{
	public DarkHammer() 
	{
		super("dm_hammer", (byte)2, new String[] {});
		
		this.peToolMaterial = "dm_tools";
		this.pePrimaryToolClass = "hammer";
		this.harvestMaterials.add(Material.iron);
		this.harvestMaterials.add(Material.anvil);
		this.harvestMaterials.add(Material.stone);
		this.harvestMaterials.add(Material.obsidian);
		this.harvestMaterials.add(Material.netherrack);
		this.harvestBlocks.add(ObjHandler.matterBlock);
		this.harvestBlocks.add(ObjHandler.dmFurnaceOff);
		this.harvestBlocks.add(ObjHandler.dmFurnaceOn);

		this.secondaryClasses.add("pickaxe");
		this.secondaryClasses.add("chisel");
	}

	// Only for RedHammer
	protected DarkHammer(String name, byte numCharges, String[] modeDesc)
	{
		super(name, numCharges, modeDesc);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase damaged, EntityLivingBase damager)
	{
		// Hammers always deal their full listed damage (dark 15, red 20),
		// independent of the legacy useOldDamage config.
		attackWithCharge(stack, damaged, damager, getAttackDamage());
		return true;
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		digAOE(stack, world, player, true, 0);
		return true;
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
	public float getAttackDamage()
	{
		return 15.0F;
	}

	@Override
	public boolean hasAttributeDamage()
	{
		return !ProjectEConfig.useOldDamage;
	}

	@Override
	public int getNumComponentsForDurability()
	{
		return 5;
	}
}
