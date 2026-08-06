package moze_intel.projecte.gameObjs.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.tiles.DMFurnaceTile;
import moze_intel.projecte.gameObjs.tiles.RMFurnaceTile;
import moze_intel.projecte.gameObjs.tiles.TileEmc;
import moze_intel.projecte.utils.ComparatorHelper;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.Block;
import net.minecraft.EnumParticle;
import net.minecraft.BlockConstants;
import net.minecraft.Material;
import net.minecraft.IconRegister;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.TileEntity;
import net.minecraft.Icon;
import net.minecraft.World;

import java.util.Random;

public class MatterFurnace extends BlockDirection
{
	private String textureName;
	private boolean isActive;
	private boolean isHighTier;
	private static boolean isUpdating;
	@SideOnly(Side.CLIENT) 
	private Icon front;
	private Random rand = new Random();

	public MatterFurnace(boolean active, boolean isRM) 
	{
		super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextBlockID(), Material.stone, new BlockConstants());
		this.setCreativeTab(ObjHandler.cTab);
		isActive = active;
		isHighTier = isRM;
		textureName = isHighTier ? "rm" : "dm";
		this.setUnlocalizedName("pe_" + textureName + "_furnace");
		
		if (isActive) 
		{
			this.setCreativeTab(null);
			this.setLightValue(0.875F);
		}
	}
	
	@Override
	public float getBlockHardness(int metadata)
	{
		return metadata == 0 ? 1000000F : 2000000F;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, net.minecraft.EnumFace face, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			if (isHighTier)
			{
				moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.RM_FURNACE_GUI, world, x, y, z);
			}
			else
			{
				moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.DM_FURNACE_GUI, world, x, y, z);
			}
		}
		
		return true;
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, int blockID, int noclue)
	{
		if (!isUpdating)
		{
			IInventory tile = (IInventory) world.getBlockTileEntity(x, y, z);
			if (tile == null) return;
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
		}
		
		world.removeBlockTileEntity(x, y, z);
	}
	
	public void updateFurnaceBlockState(boolean isActive, World world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		isUpdating = true;

		if (isActive)
		{
			if (isHighTier)
				world.setBlock(x, y, z, ObjHandler.rmFurnaceOn.blockID);
			else
				world.setBlock(x, y, z, ObjHandler.dmFurnaceOn.blockID);
		}
		else
		{
			if (isHighTier)
				world.setBlock(x, y, z, ObjHandler.rmFurnaceOff.blockID);
			else
				world.setBlock(x, y, z, ObjHandler.dmFurnaceOff.blockID);
		}

		isUpdating = false;
		world.setBlockMetadataWithNotify(x, y, z, meta, 2);

		if (tile != null)
		{
			tile.validate();
			world.setBlockTileEntity(x, y, z, tile);
		}
	}
	
	@Override
	public boolean onBlockPlacedMITE(World world, int x, int y, int z, int metadata, net.minecraft.Entity placer, boolean test_only)
	{
		setFacingMeta(world, x, y, z, ((EntityPlayer) placer));
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand)
	{
		if (isActive)
		{
			int l = world.getBlockMetadata(x, y, z);
			float f = (float) x + 0.5F;
			float f1 = (float) y + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
			float f2 = (float) z + 0.5F;
			float f3 = 0.52F;
			float f4 = rand.nextFloat() * 0.6F - 0.3F;

			if (l == 4)
			{
				world.spawnParticle(EnumParticle.smoke, (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticle.flame, (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 5)
			{
				world.spawnParticle(EnumParticle.smoke, (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticle.flame, (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 2)
			{
				world.spawnParticle(EnumParticle.smoke, (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticle.flame, (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 3)
			{
				world.spawnParticle(EnumParticle.smoke, (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticle.flame, (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		this.blockIcon = register.registerIcon("projecte:" + textureName);
		front = register.registerIcon("projecte:matter_furnace/" + (isActive ? (textureName + "_on") : (textureName + "_off")));
	}
	
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (meta == 0 && side == 3) 
		{
			return front;
		}
		
		return side != meta ? this.blockIcon : front;
	}
	
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z)
	{
		return isHighTier ? net.minecraft.Item.getItem(ObjHandler.rmFurnaceOff) : net.minecraft.Item.getItem(ObjHandler.dmFurnaceOff);
	}

	@Override
	public boolean hasTileEntity()
	{
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return isHighTier ? new RMFurnaceTile() : new DMFurnaceTile();
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int meta)
	{
		return ComparatorHelper.getForMatterFurnace(world, x, y, z);
	}
}
