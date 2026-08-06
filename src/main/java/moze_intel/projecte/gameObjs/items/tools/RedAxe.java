package moze_intel.projecte.gameObjs.items.tools;

import net.minecraft.Material;

public class RedAxe extends DarkAxe
{
	public RedAxe()
	{
		super("rm_axe", (byte)3, new String[]{});
		
		this.peToolMaterial = "rm_tools";
		this.pePrimaryToolClass = "axe";
		this.harvestMaterials.add(Material.wood);
		this.harvestMaterials.add(Material.plants);
		this.harvestMaterials.add(Material.vine);
	}

	@Override
	public float getAttackDamage()
	{
		return 9;
	}
}
