package moze_intel.projecte.gameObjs.items.tools;

import net.minecraft.Material;

public class RedShovel extends DarkShovel
{
	public RedShovel() 
	{
		super("rm_shovel", (byte)3, new String[]{});
		
		this.peToolMaterial = "rm_tools";
		this.pePrimaryToolClass = "shovel";
		this.harvestMaterials.add(Material.grass);
		this.harvestMaterials.add(Material.dirt);
		this.harvestMaterials.add(Material.sand);
		this.harvestMaterials.add(Material.snow);
		this.harvestMaterials.add(Material.clay);
	}

	@Override
	public float getAttackDamage()
	{
		return 6;
	}
}
