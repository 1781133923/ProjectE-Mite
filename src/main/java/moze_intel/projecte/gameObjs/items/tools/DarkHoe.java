package moze_intel.projecte.gameObjs.items.tools;

import net.minecraft.Material;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumFace;
import net.minecraft.ItemStack;
import net.minecraft.RaycastCollision;
import net.minecraft.World;

public class DarkHoe extends PEToolBase
{
	public DarkHoe() 
	{
		super("dm_hoe", (byte)2, new String[]{});
		
		this.peToolMaterial = "dm_tools";
		this.pePrimaryToolClass = "hoe";
		this.harvestMaterials.add(Material.grass);
		this.harvestMaterials.add(Material.dirt);
		this.harvestMaterials.add(Material.sand);
		this.harvestMaterials.add(Material.snow);
		this.harvestMaterials.add(Material.craftedSnow);
		this.harvestMaterials.add(Material.cake);
	}

	// Only for RedHoe
	protected DarkHoe(String name, byte numCharges, String[] modeDesc)
	{
		super(name, numCharges, modeDesc);
	}

	// MITE never calls the 1.7.10-style onItemUse; right-click actions go
	// through onItemRightClick with a RaycastCollision (same as MITE's hoe).
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down)
	{
		ItemStack stack = player.getHeldItemStack();
		RaycastCollision rc = player.getSelectedObject(partial_tick, true);
		if (rc == null || !rc.isBlock() || rc.face_hit == null || !rc.face_hit.isTop())
		{
			return false;
		}

		// Till the clicked block and the charge-sized area around it; every
		// tilled block also becomes fertilized farmland (see tillAOE).
		tillAOE(stack, player, rc.world, rc.block_hit_x, rc.block_hit_y, rc.block_hit_z, 0, 0);
		return true;
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
