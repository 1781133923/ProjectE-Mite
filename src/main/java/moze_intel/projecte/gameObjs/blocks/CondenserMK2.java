package moze_intel.projecte.gameObjs.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.tiles.CondenserMK2Tile;
import moze_intel.projecte.utils.Constants;
import net.minecraft.IconRegister;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.TileEntity;
import net.minecraft.World;

import java.util.Random;

public class CondenserMK2 extends Condenser
{
	public CondenserMK2()
	{
		super();
		this.setUnlocalizedName("pe_condenser_mk2");
	}

	@Override
	public int getRenderType()
	{
		return 22;
	}

	@Override
	public boolean hasTileEntity()
	{
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new CondenserMK2Tile();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, net.minecraft.EnumFace face, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.CONDENSER_MK2_GUI, world, x, y, z);
		}

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		this.blockIcon = register.registerIcon("projecte:condenser_mk2_side");
	}
}
