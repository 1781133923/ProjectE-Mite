package moze_intel.projecte.gameObjs.items.tools;

import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.Material;

public class RedHammer extends DarkHammer
{
	public RedHammer() 
	{
		super("rm_hammer", (byte)3, new String[]{});
		
		this.peToolMaterial = "rm_tools";
		this.pePrimaryToolClass = "hammer";
		this.harvestMaterials.add(Material.iron);
		this.harvestMaterials.add(Material.anvil);
		this.harvestMaterials.add(Material.stone);
		this.harvestMaterials.add(Material.obsidian);
		this.harvestMaterials.add(Material.netherrack);
		this.harvestBlocks.add(ObjHandler.matterBlock);
		this.harvestBlocks.add(ObjHandler.rmFurnaceOff);
		this.harvestBlocks.add(ObjHandler.rmFurnaceOn);

		this.secondaryClasses.add("pickaxe");
		this.secondaryClasses.add("chisel");
	}

	@Override
	public float getAttackDamage()
	{
		return 20.0F;
	}
}
