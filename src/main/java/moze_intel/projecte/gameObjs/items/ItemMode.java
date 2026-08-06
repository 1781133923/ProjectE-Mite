package moze_intel.projecte.gameObjs.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.api.item.IModeChanger;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.ChatComponentTranslation;
import net.minecraft.EnumChatFormatting;
import net.minecraft.StatCollector;

import java.util.List;

public abstract class ItemMode extends ItemCharge implements IModeChanger
{
	private byte numModes;
	private String[] modes;
	
	public ItemMode(String unlocalName, byte numCharge, String[] modeDescrp)
	{
		super(unlocalName, numCharge);
		this.numModes = (byte) modeDescrp.length;
		// Store the raw translation keys: language files are loaded after item
		// construction, so translating here would leave the raw "pe.xxx.modeN"
		// keys in the mode description.
		this.modes = modeDescrp;
	}
	
	public byte getMode(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new net.minecraft.NBTTagCompound();
		}
		return stack.stackTagCompound.getByte("Mode");
	}
	
	public String getModeDescription(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new net.minecraft.NBTTagCompound();
		}
		return StatCollector.translateToLocal(modes[stack.stackTagCompound.getByte("Mode")]);
	}
	
	protected void changeMode(ItemStack stack)
	{
		byte newMode = (byte) (getMode(stack) + 1);
		stack.stackTagCompound.setByte("Mode", (newMode > numModes - 1 ? 0 : newMode));
	}
	
	@Override
	public void changeMode(EntityPlayer player, ItemStack stack)
	{
		if (numModes == 0)
		{
			return;
		}
		changeMode(stack);
		moze_intel.projecte.compat.PEChatHelper.send(player, new ChatComponentTranslation("pe.item.mode_switch", getModeDescription(stack)));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4, net.minecraft.Slot slot) 
	{
		if (stack.hasTagCompound() && this.numModes > 0)
		{
			list.add(StatCollector.translateToLocal("pe.item.mode") + ": " + EnumChatFormatting.AQUA + getModeDescription(stack));
		}
	}
}
