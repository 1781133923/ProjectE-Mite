package moze_intel.projecte.gameObjs.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.tiles.CondenserTile;
import moze_intel.projecte.utils.ComparatorHelper;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.Block;
import net.minecraft.IconRegister;
import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.TileEntity;
import net.minecraft.World;

import java.util.Random;

public class Condenser extends AlchemicalChest
{
	public Condenser() 
	{
		super();
		this.setUnlocalizedName("pe_condenser");
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
		return new CondenserTile();
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, net.minecraft.EnumFace face, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote) 
		{
			moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.CONDENSER_GUI, world, x, y, z);
		}
		
		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int blockID, int noclue)
	{
		IInventory tile = (IInventory) world.getBlockTileEntity(x, y, z);

		if (tile == null)
		{
			return;
		}

		for (int i = 1; i < tile.getSizeInventory(); i++)
		{
			ItemStack stack = tile.getStackInSlot(i);

			if (stack == null)
			{
				continue;
			}

			WorldHelper.spawnEntityItem(world, stack, x, y, z);
		}

		world.notifyBlocksOfNeighborChange(x, y, z, blockID);
		world.removeBlockTileEntity(x, y, z);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		this.blockIcon = register.registerIcon("projecte:condenser_side");
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int meta)
	{
		return ComparatorHelper.getForCondenser(world, x, y, z);
	}
}
