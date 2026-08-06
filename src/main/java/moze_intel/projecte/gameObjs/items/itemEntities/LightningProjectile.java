package moze_intel.projecte.gameObjs.items.itemEntities;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.gameObjs.items.ItemPE;
import net.minecraft.IconRegister;

public class LightningProjectile extends ItemPE
{
	public LightningProjectile()
	{
		setCreativeTab(null);
		setUnlocalizedName("wind_projectile");
		setMaxStackSize(1);
	}

	@Override
	public void registerIcons(IconRegister register)
	{
		itemIcon = register.registerIcon(getTexture("entities", "lightning"));
	}
}
