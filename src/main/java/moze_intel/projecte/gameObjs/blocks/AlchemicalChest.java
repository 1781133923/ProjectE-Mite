package moze_intel.projecte.gameObjs.blocks;


import net.minecraft.BlockConstants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.tiles.AlchChestTile;
import moze_intel.projecte.utils.ComparatorHelper;
import moze_intel.projecte.utils.Constants;
import net.minecraft.Material;
import net.minecraft.IconRegister;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.TileEntity;
import net.minecraft.World;
import net.minecraft.IBlockAccess;

import java.util.Random;

public class AlchemicalChest extends BlockDirection
{
	public AlchemicalChest() 
	{
		super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextBlockID(), Material.stone, new BlockConstants());
		this.setUnlocalizedName("pe_alchemy_chest");
		this.setBlockBoundsForAllThreads(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
		this.setHardness(10.0f);
		this.setResistance(6000000.0F);
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
	{
		// Always render every face (including the bottom) so the non-full-cube
		// shape does not leave a see-through gap at the ground contact face.
		return true;
	}

	@Override
	public boolean isStandardFormCube(boolean[] isSolid, int metadata)
	{
		// Not a full cube: tell MITE so neighbouring blocks do not have their
		// shared face culled (which would make the contact face see-through).
		return false;
	}
	
	@Override
	public int getRenderType()
	{
		// MITE's chest render type: renders as a 3D held item and lets the
		// tile-entity renderer draw the model in the world.
		return 22;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, net.minecraft.EnumFace face, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote) 
		{
			moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.ALCH_CHEST_GUI, world, x, y, z);
		}
		
		return true;
	}

	@Override
	public boolean hasTileEntity()
	{
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new AlchChestTile();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		this.blockIcon = register.registerIcon("projecte:alchemy_chest_side");
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int meta)
	{
		return ComparatorHelper.getForAlchChest(world, x, y, z);
	}
}
