package moze_intel.projecte.gameObjs.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.IconRegister;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.EnumChatFormatting;
import net.minecraft.StatCollector;

import java.util.List;

public class DiviningRodMedium extends DiviningRodLow
{
	public DiviningRodMedium()
	{
		super(new String[]{"3x3x3", "16x3x3"});
		this.setUnlocalizedName("divining_rod_2");
	}

	// Only for subclasses
	protected DiviningRodMedium(String[] modeDesc)
	{
		super(modeDesc);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4, net.minecraft.Slot slot) 
	{
		if (stack.hasTagCompound())
		{
			list.add(StatCollector.translateToLocal("pe.item.mode") + ": " + EnumChatFormatting.AQUA + modes[getMode(stack)]);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		this.itemIcon = register.registerIcon(this.getTexture("divining2"));
	}
}
