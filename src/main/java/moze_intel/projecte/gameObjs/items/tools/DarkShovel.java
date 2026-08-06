package moze_intel.projecte.gameObjs.items.tools;

import net.minecraft.Material;
import net.minecraft.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.ItemStack;
import net.minecraft.MovingObjectPosition;
import net.minecraft.World;

public class DarkShovel extends PEToolBase
{
	public DarkShovel() 
	{
		super("dm_shovel", (byte)1, new String[]{});
		
		this.peToolMaterial = "dm_tools";
		this.pePrimaryToolClass = "shovel";
		this.harvestMaterials.add(Material.grass);
		this.harvestMaterials.add(Material.dirt);
		this.harvestMaterials.add(Material.sand);
		this.harvestMaterials.add(Material.snow);
		this.harvestMaterials.add(Material.clay);
	}

	// Only for RedShovel
	protected DarkShovel(String name, byte numCharges, String[] modeDesc)
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

		MovingObjectPosition mop = moze_intel.projecte.compat.PECompatHelper.getMovingObjectPositionFromPlayer(world, player);
		if (mop != null && mop.getEntityHit() == null
				&& world.getBlock(mop.blockX, mop.blockY, mop.blockZ) == Blocks.gravel)
		{
			tryVeinMine(stack, player, mop);
		}
		else
		{
			digAOE(stack, world, player, false, 0);
		}
		return true;
	}

	@Override
	public float getAttackDamage()
	{
		return this instanceof RedShovel ? 6 : 5;
	}
}
