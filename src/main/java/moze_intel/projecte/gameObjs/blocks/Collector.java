package moze_intel.projecte.gameObjs.blocks;


import net.minecraft.BlockConstants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.tiles.CollectorMK1Tile;
import moze_intel.projecte.gameObjs.tiles.CollectorMK2Tile;
import moze_intel.projecte.gameObjs.tiles.CollectorMK3Tile;
import moze_intel.projecte.gameObjs.tiles.TileEmc;
import moze_intel.projecte.utils.ComparatorHelper;
import moze_intel.projecte.utils.Constants;
import net.minecraft.Material;
import net.minecraft.IconRegister;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.TileEntity;
import net.minecraft.Icon;
import net.minecraft.IBlockAccess;
import net.minecraft.World;
import net.minecraftforge.common.util.ForgeDirection;

public class Collector extends BlockDirection
{
	@SideOnly(Side.CLIENT)
	private Icon front;
	@SideOnly(Side.CLIENT)
	private Icon top;
	private int tier;
	
	public Collector(int tier) 
	{
		super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextBlockID(), Material.glass, new BlockConstants());
		this.setUnlocalizedName("pe_collector_MK" + tier);
		this.setLightValue(Constants.COLLECTOR_LIGHT_VALS[tier - 1]);
		this.setHardness(0.3f);
		this.tier = tier;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, net.minecraft.EnumFace face, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
			switch (tier)
			{
				case 1:
					moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.COLLECTOR1_GUI, world, x, y, z);
					break;
				case 2:
					moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.COLLECTOR2_GUI, world, x, y, z);
					break;
				case 3:
					moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.COLLECTOR3_GUI, world, x, y, z);
					break;
			}
		return true;
	}
	
	@Override
	public boolean onBlockPlacedMITE(World world, int x, int y, int z, int metadata, net.minecraft.Entity placer, boolean test_only)
	{
		setFacingMeta(world, x, y, z, ((EntityPlayer) placer));
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		this.blockIcon = register.registerIcon("projecte:collectors/other");
		this.front = register.registerIcon("projecte:collectors/front");
		this.top = register.registerIcon("projecte:collectors/top_"+Integer.toString(tier));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (meta == 0 && side == 3) 
		{
			return front;
		}
		
		if (side == 1) 
		{
			return top;
		}
		
		return side != meta ? this.blockIcon : front;
	}

	@Override
	public boolean hasTileEntity()
	{
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		switch (tier) {
			case 3:
				return new CollectorMK3Tile();
			case 2:
				return new CollectorMK2Tile();
			case 1:
				return new CollectorMK1Tile();
			default:
				return null;
		}
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int meta)
	{
		return ComparatorHelper.getForCollector(world, x, y, z);
	}

}
