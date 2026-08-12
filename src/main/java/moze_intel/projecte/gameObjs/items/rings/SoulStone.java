package moze_intel.projecte.gameObjs.items.rings;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.Optional;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.handlers.PlayerTimers;
import moze_intel.projecte.utils.MathUtils;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.ItemStack;
import net.minecraft.EnumChatFormatting;
import net.minecraft.StatCollector;
import net.minecraft.World;

import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class SoulStone extends RingToggle implements IBauble, IPedestalItem
{
	private int healCooldown;

	public SoulStone()
	{
		super("soul_stone");
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5)
	{
		if (world.isRemote || !(entity instanceof EntityPlayer)) 
		{
			return;
		}
		
		super.onUpdate(stack, world, entity, par4, par5);
		
		EntityPlayer player = (EntityPlayer) entity;

		// Carried effect mirrors the gem helmet: always active, costs no fuel
		// and needs no toggle (see BodyStone for the fuel auto-deactivation
		// problem that made carried stones appear broken).
		PlayerTimers.activateHeal(player);

		if (player.getHealth() < player.getMaxHealth() && PlayerTimers.canHeal(player))
		{
			world.playSoundAtEntity(player, "projecte:item.peheal", 1.0F, 1.0F);
			player.heal(1.0F); // half a heart per 20 ticks
		}
	}
	
	@Override
	public void changeMode(EntityPlayer player, ItemStack stack)
	{
		super.changeMode(player, stack);
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public baubles.api.BaubleType getBaubleType(ItemStack itemstack)
	{
		return BaubleType.AMULET;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onWornTick(ItemStack stack, EntityLivingBase player) 
	{
		this.onUpdate(stack, player.worldObj, player, 0, false);
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onEquipped(ItemStack itemstack, EntityLivingBase player) {}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {}

	@Override
	@Optional.Method(modid = "Baubles")
	public boolean canEquip(ItemStack itemstack, EntityLivingBase player) 
	{
		return true;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) 
	{
		return true;
	}

	@Override
	public void updateInPedestal(World world, int x, int y, int z)
	{
		if (!world.isRemote && ProjectEConfig.soulPedCooldown != -1)
		{
			DMPedestalTile tile = ((DMPedestalTile) world.getBlockTileEntity(x, y, z));
			if (tile.getActivityCooldown() == 0)
			{
				List<ServerPlayer> players = world.getEntitiesWithinAABB(ServerPlayer.class, tile.getEffectBounds());

				for (ServerPlayer player : players)
				{
					if (player.getHealth() < player.getMaxHealth())
					{
						world.playSoundAtEntity(player, "projecte:item.peheal", 1.0F, 1.0F);
						player.heal(1.0F); // 1/2 heart
					}
				}

				tile.setActivityCooldown(ProjectEConfig.soulPedCooldown);
			}
			else
			{
				tile.decrementActivityCooldown();
			}
		}
	}

	@Override
	public List<String> getPedestalDescription()
	{
		List<String> list = Lists.newArrayList();
		if (ProjectEConfig.soulPedCooldown != -1)
		{
			list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.soul.pedestal1"));
			list.add(EnumChatFormatting.BLUE + String.format(
					StatCollector.translateToLocal("pe.soul.pedestal2"), MathUtils.tickToSecFormatted(ProjectEConfig.soulPedCooldown)));
		}
		return list;
	}

	@Override
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List list, boolean par4, net.minecraft.Slot slot)
	{
		super.addInformation(stack, player, list, par4, slot);
		list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.soul.tooltip1"));
	}
}
