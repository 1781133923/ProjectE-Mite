package moze_intel.projecte.gameObjs.items.itemEntities;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.gameObjs.items.ItemPE;
import net.minecraft.IconRegister;

public class FireProjectile extends ItemPE
{
	public FireProjectile()
	{
		setCreativeTab(null);
		setUnlocalizedName("fire_projectile");
		setMaxStackSize(1);
	}

	@Override
	public void registerIcons(IconRegister register)
	{
		itemIcon = register.registerIcon(getTexture("entities", "fireball"));
	}
}
