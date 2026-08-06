package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.gameObjs.ObjHandler;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.Material;
import net.minecraft.IconRegister;
import net.minecraft.CreativeTabs;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Icon;
import net.minecraft.MathHelper;

public class FuelBlock extends Block 
{
	@SideOnly(Side.CLIENT)
	private Icon icons[];
	
	public FuelBlock() 
	{
		super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextBlockID(), Material.stone, new BlockConstants());
		this.setUnlocalizedName("pe_fuel_block");
		this.setCreativeTab(ObjHandler.cTab);
		this.setHardness(0.5f);
	}

	// MITE derives the block's item subtypes in the Block constructor from
	// isValidMetadata(): without this override only metadata 0 is considered a
	// valid item, so the Mobius/Aeternalis fuel block subtypes never show up in
	// the creative tab or EMI (they can still be crafted).
	@Override
	public boolean isValidMetadata(int metadata)
	{
		return metadata >= 0 && metadata < 3;
	}

	// MITE builds the block's item-subtype list from getItemSubtype(metadata),
	// which by default maps every valid metadata to subtype 0 - so even with
	// isValidMetadata() fixed, all three fuel blocks would collapse into the
	// alchemical coal block. Here metadata IS the item subtype.
	@Override
	public int getBlockSubtypeUnchecked(int metadata)
	{
		return metadata;
	}
	
	public int damageDropped(int meta)
	{
		return meta;
	}

	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item fuelBlock, CreativeTabs cTab, List list)
	{
		for (int i = 0; i < 3; i++)
		{
			list.add(new ItemStack(fuelBlock , 1, i));
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		icons = new Icon[3];
		
		for (int i = 0; i < 3; i++)
		{
			icons[i] = register.registerIcon("projecte:fuels_"+i);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		return icons[MathHelper.clamp_int(meta, 0, 2)];
	}
}
