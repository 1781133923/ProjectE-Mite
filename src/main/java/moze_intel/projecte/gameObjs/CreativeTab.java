package moze_intel.projecte.gameObjs;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import huix.glacier.api.extension.creativetab.GlacierCreativeTabs;
import net.minecraft.Item;

/**
 * Uses RustedIronCore's GlacierCreativeTabs so the tab is registered in the
 * creative tab list and rendered by RIC's paged creative inventory GUI.
 */
public class CreativeTab extends GlacierCreativeTabs
{
	public CreativeTab()
	{
		super("ProjectE");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() 
	{
		return ObjHandler.philosStone;
	}
}
