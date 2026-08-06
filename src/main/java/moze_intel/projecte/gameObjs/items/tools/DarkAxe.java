package moze_intel.projecte.gameObjs.items.tools;

import net.minecraft.Material;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.World;

public class DarkAxe extends PEToolBase
{
	public DarkAxe()
	{
		super("dm_axe", (byte)2, new String[]{});
		
		this.peToolMaterial = "dm_tools";
		this.pePrimaryToolClass = "axe";
		this.harvestMaterials.add(Material.wood);
		this.harvestMaterials.add(Material.plants);
		this.harvestMaterials.add(Material.vine);
	}

	// Only for RedAxe
	protected DarkAxe(String name, byte numCharges, String[] modeDesc)
	{
		super(name, numCharges, modeDesc);
	}
	
	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		clearOdAOE(world, stack, player, "logWood", 0);
		clearOdAOE(world, stack, player, "treeLeaves", 0);
		return true;
	}

	@Override
	public float getAttackDamage()
	{
		return this instanceof RedAxe ? 9 : 8;
	}
}
