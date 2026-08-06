package moze_intel.projecte.gameObjs.items.rings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.api.item.IModeChanger;
import moze_intel.projecte.gameObjs.items.ItemPE;
import net.minecraft.IconRegister;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.Icon;
import net.minecraft.World;

public abstract class RingToggle extends ItemPE implements IModeChanger
{
	private String name;
	@SideOnly(Side.CLIENT)
	private Icon ringOn;
	@SideOnly(Side.CLIENT)
	private Icon ringOff;
	
	public RingToggle(String unlocalName)
	{
		name = unlocalName;
		this.setUnlocalizedName(unlocalName);
		this.setMaxStackSize(1);
					}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) 
	{
		if (!stack.hasTagCompound())
		{
			stack.setTagCompound(new NBTTagCompound());
		}
	}

	public boolean showDurabilityBar(ItemStack stack)
	{
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public Icon getIconFromSubtype(int dmg)
	{
		return dmg == 0 ? ringOff : ringOn;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		ringOn = register.registerIcon(this.getTexture("rings", name+"_on"));
		ringOff = register.registerIcon(this.getTexture("rings", name+"_off"));
	}

	@Override
	public byte getMode(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new NBTTagCompound();
		}
		return stack.stackTagCompound.getByte("Mode");
	}

	protected void setMode(ItemStack stack, int mode)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new NBTTagCompound();
		}
		stack.stackTagCompound.setByte("Mode", (byte) mode);
	}

	@Override
	public void changeMode(EntityPlayer player, ItemStack stack)
	{
		if (getMode(stack) == 0)
		{
			player.worldObj.playSoundAtEntity(player, "projecte:item.peheal", 1.0F, 1.0F);
			setMode(stack, 1);
		}
		else
		{
			player.worldObj.playSoundAtEntity(player, "projecte:item.peuncharge", 1.0F, 1.0F);
			setMode(stack, 0);
		}
	}
}
