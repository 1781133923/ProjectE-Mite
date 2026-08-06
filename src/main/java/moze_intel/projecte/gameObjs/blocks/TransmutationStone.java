package moze_intel.projecte.gameObjs.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.tiles.TileEmc;
import moze_intel.projecte.utils.Constants;
import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.Material;
import net.minecraft.IconRegister;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.TileEntity;
import net.minecraft.Icon;
import net.minecraft.World;

import java.util.Random;

public class TransmutationStone extends Block
{
	@SideOnly(Side.CLIENT)
	private Icon[] icon;
	
	public TransmutationStone() 
	{
		super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextBlockID(), Material.stone, new BlockConstants());
		this.setCreativeTab(ObjHandler.cTab);
		this.setUnlocalizedName("pe_transmutation_stone");
		this.setBlockBoundsForAllThreads(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
		this.setHardness(10.0f);
	}

	@Override
	public boolean isStandardFormCube(boolean[] isSolid, int metadata)
	{
		// Keep the low "table" shape instead of being treated as a full cube.
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, net.minecraft.EnumFace face, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.TRANSMUTATION_GUI, world, x, y, z);
		}
		return true;
	}
	
	@Override
	public boolean onBlockPlacedMITE(World world, int x, int y, int z, int metadata, net.minecraft.Entity placer, boolean test_only)
	{
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side < 2)
		{
			return icon[side];
		}
		return icon[2];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		icon = new Icon[3];
		icon[0] = register.registerIcon("projecte:transmutation_stone/bottom");
		icon[1] = register.registerIcon("projecte:transmutation_stone/top");
		icon[2] = register.registerIcon("projecte:transmutation_stone/side");
	}

}
