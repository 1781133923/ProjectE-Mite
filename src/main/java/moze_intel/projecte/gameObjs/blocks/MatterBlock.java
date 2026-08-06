package moze_intel.projecte.gameObjs.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.Material;
import net.minecraft.IconRegister;
import net.minecraft.CreativeTabs;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Icon;
import net.minecraft.World;

import java.util.List;

public class MatterBlock extends Block
{
	@SideOnly(Side.CLIENT)
	private Icon dmIcon;
	@SideOnly(Side.CLIENT)
	private Icon rmIcon;
	
	public MatterBlock() 
	{
		super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextBlockID(), Material.iron, new BlockConstants());
		this.setCreativeTab(ObjHandler.cTab);
		this.setUnlocalizedName("pe_matter_block");
	}

	// Same as FuelBlock: MITE only registers block item subtypes whose metadata
	// passes isValidMetadata(); without this the red matter block (meta 1) is
	// missing from the creative tab / EMI.
	@Override
	public boolean isValidMetadata(int metadata)
	{
		return metadata >= 0 && metadata < 2;
	}

	// See FuelBlock: without this every valid metadata maps to subtype 0 and
	// the red matter block collapses into the dark matter block.
	@Override
	public int getBlockSubtypeUnchecked(int metadata)
	{
		return metadata;
	}
	
	@Override
	public float getBlockHardness(int metadata)
	{
		int meta = metadata;
		
		if (meta == 0) 
		{
			return 1000000.0F;
		}
		else
		{
			return 2000000.0F;
		}
	}
	
	public boolean canHarvestBlock(EntityPlayer player, int meta)
	{
		ItemStack stack = player.getHeldItemStack();
		
		if (stack != null)
		{
			if (meta == 1)
			{
				return stack.getItem() == ObjHandler.rmPick || stack.getItem() == ObjHandler.rmStar;
			}
			else
			{
				return stack.getItem() == ObjHandler.rmPick || stack.getItem() == ObjHandler.dmPick || stack.getItem() == ObjHandler.rmStar;
			}
		}
		
		return false;
	}
	
	public int damageDropped(int meta)
	{
		return meta;
	}

	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item matterBlock, CreativeTabs cTab, List list)
	{
		for (int i = 0; i <= 1; i++)
		{
			list.add(new ItemStack(matterBlock , 1, i));
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		dmIcon = register.registerIcon("projecte:dm");
		rmIcon = register.registerIcon("projecte:rm");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (meta == 0) 
		{
			return dmIcon;
		}
		else return rmIcon;
	}
	
}
