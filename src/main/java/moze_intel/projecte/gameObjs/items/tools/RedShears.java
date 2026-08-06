package moze_intel.projecte.gameObjs.items.tools;

import net.minecraft.Material;

public class RedShears extends DarkShears
{
	public RedShears()
	{
		super("rm_shears", (byte) 3, new String[]{});
		
		this.peToolMaterial = "rm_tools";
		this.pePrimaryToolClass = "shears";
		this.harvestMaterials.add(Material.web);
		this.harvestMaterials.add(Material.circuits);
		this.harvestMaterials.add(Material.cloth);
		this.harvestMaterials.add(Material.plants);
		this.harvestMaterials.add(Material.tree_leaves);
		this.harvestMaterials.add(Material.vine);
	}

	@Override
	public float getAttackDamage()
	{
		return 10.0F;
	}
}
