package moze_intel.projecte.gameObjs.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.utils.AchievementHandler;
import moze_intel.projecte.utils.Constants;
import net.minecraft.IconRegister;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.World;

public class TransmutationTablet extends ItemPE
{
	public TransmutationTablet()
	{
		this.setUnlocalizedName("transmutation_tablet");
		this.setMaxStackSize(1);
	}
	
	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if (!world.isRemote)
		{
			moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.TRANSMUTATION_GUI, world, (int) player.posX, (int) player.posY, (int) player.posZ);
		}
		
		return true;
	}
	
	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) 
	{
		super.onCreated(stack, world, player);
		
		if (!world.isRemote)
		{
			player.addStat(AchievementHandler.PORTABLE_TRANSMUTATION, 1);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		this.itemIcon = register.registerIcon(this.getTexture("transmute_tablet"));
	}
}
