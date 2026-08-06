package moze_intel.projecte.gameObjs.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.entity.EntityNovaCatalystPrimed;
import net.minecraft.BlockTNT;
import net.minecraft.IconRegister;
import net.minecraft.EntityLivingBase;
import net.minecraft.Icon;
import net.minecraft.Explosion;
import net.minecraft.World;

public class NovaCatalyst extends BlockTNT
{
	@SideOnly(Side.CLIENT)
	protected Icon topIcon;
	@SideOnly(Side.CLIENT)
	protected Icon bottomIcon;
	
	public NovaCatalyst()
	{
		super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextBlockID());
		this.setUnlocalizedName("pe_nova_catalyst");
		this.setCreativeTab(ObjHandler.cTab);
	}
	
	public void func_150114_a(World world, int x, int y, int z, int par5, EntityLivingBase entity)
	{
		if (!world.isRemote && par5 == 1)
		{
			BlockTNT.ignite(world, x, y, z, entity);
		}
	}
	
	@Override
	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion)
	{
		func_150114_a(world, x, y, z, 1, null);
	}
	
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int p_149691_1_, int p_149691_2_)
	{
		return p_149691_1_ == 0 ? bottomIcon : (p_149691_1_ == 1 ? topIcon : this.blockIcon);
	}
	
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		this.blockIcon = register.registerIcon("projecte:explosives/nova_side");
		topIcon = register.registerIcon("projecte:explosives/top");
		bottomIcon = register.registerIcon("projecte:explosives/bottom");
	}
}
