package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.tiles.TileEmc;
import moze_intel.projecte.gameObjs.tiles.TileEmcDirection;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.Block;
import net.minecraft.BlockContainer;
import net.minecraft.BlockConstants;
import net.minecraft.Material;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.ItemStack;
import net.minecraft.TileEntity;
import net.minecraft.MathHelper;
import net.minecraft.World;

public abstract class BlockDirection extends BlockContainer
{
	public BlockDirection(int blockID, Material material, BlockConstants constants)
	{
		super(blockID, material, constants);
		this.setCreativeTab(ObjHandler.cTab);
	}
	
	@Override
	public boolean onBlockPlacedMITE(World world, int x, int y, int z, int metadata, net.minecraft.Entity placer, boolean test_only)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		
		if (tile instanceof TileEmcDirection && placer instanceof EntityLivingBase)
		{
			((TileEmcDirection) tile).setRelativeOrientation((EntityLivingBase) placer, false);
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
		
		for (int i = 0; i < tile.getSizeInventory(); i++)
		{
			ItemStack stack = tile.getStackInSlot(i);
			
			if (stack == null)
			{
				continue;
			}
			
			WorldHelper.spawnEntityItem(world, stack, x, y, z);
		}
		
		world.notifyBlocksOfNeighborChange(x, y, z, blockID);
		super.breakBlock(world, x, y, z, blockID, noclue);
	}
	
	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) 
	{
		if (world.isRemote)
		{
			return;
		}
		
		ItemStack stack = player.getHeldItemStack();
		
		if (stack != null && stack.getItem() == ObjHandler.philosStone)
		{
			TileEntity tile = world.getBlockTileEntity(x, y, z);
			
			if (tile instanceof TileEmcDirection)
			{
				((TileEmcDirection) tile).setRelativeOrientation(player, true);
			}
			else
			{
				setFacingMeta(world, x, y, z, player);
			}
		}
	}

	protected void setFacingMeta(World world, int x, int y, int z, EntityPlayer player)
	{
		switch (MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3)
		{
			case 0: world.setBlockMetadataWithNotify(x, y, z, 2, 2); break;
			case 1: world.setBlockMetadataWithNotify(x, y, z, 5, 2); break;
			case 2: world.setBlockMetadataWithNotify(x, y, z, 3, 2); break;
			case 3: world.setBlockMetadataWithNotify(x, y, z, 4, 2); break;
			default: world.setBlockMetadataWithNotify(x, y, z, 2, 2);
		}
	}

}
