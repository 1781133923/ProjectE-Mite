package moze_intel.projecte.gameObjs.blocks;


import net.minecraft.BlockConstants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.tiles.RelayMK1Tile;
import moze_intel.projecte.gameObjs.tiles.RelayMK2Tile;
import moze_intel.projecte.gameObjs.tiles.RelayMK3Tile;
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
import net.minecraft.World;

public class Relay extends BlockDirection
{
	@SideOnly(Side.CLIENT)
	private Icon front;
	@SideOnly(Side.CLIENT)
	private Icon top;
	private int tier;
	
	public Relay(int tier) 
	{
		super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextBlockID(), Material.stone, new BlockConstants());
		this.setUnlocalizedName("pe_relay_MK" + Integer.toString(tier));
		this.setLightValue(Constants.COLLECTOR_LIGHT_VALS[tier - 1]);
		this.setHardness(10.0f);
		this.tier = tier;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, net.minecraft.EnumFace face, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			switch (tier)
			{
				case 1:
					moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.RELAY1_GUI, world, x, y, z);
					break;
				case 2:
					moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.RELAY2_GUI, world, x, y, z);
					break;
				case 3:
					moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.RELAY3_GUI, world, x, y, z);
					break;
			}
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
		this.blockIcon = register.registerIcon("projecte:relays/other");
		this.front = register.registerIcon("projecte:relays/front");
		this.top = register.registerIcon("projecte:relays/top_"+Integer.toString(tier));
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
	public TileEntity createNewTileEntity(World world)
	{
		switch (tier)
		{
			case 1: return new RelayMK1Tile();
			case 2: return new RelayMK2Tile();
			case 3: return new RelayMK3Tile();
			default: return null;
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
		return ComparatorHelper.getForRelay(world, x, y, z);
	}
}
