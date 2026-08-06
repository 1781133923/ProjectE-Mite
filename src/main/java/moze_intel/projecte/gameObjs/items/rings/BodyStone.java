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
public class BodyStone extends RingToggle implements IBauble, IPedestalItem
{
	public BodyStone()
	{
		super("body_stone");
		
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5)
	{
		if (world.isRemote || par4 > 8 || !(entity instanceof EntityPlayer)) 
		{
			return;
		}
		
		super.onUpdate(stack, world, entity, par4, par5);
		
		EntityPlayer player = (EntityPlayer) entity;

		// Carried effect mirrors the gem chestplate: always active, costs no
		// fuel and needs no toggle. The old code silently deactivated the
		// stone whenever the player carried no alchemical fuel/Klein star,
		// which made the carried effect look completely broken.
		PlayerTimers.activateSlowFeed(player);

		if (player.getFoodStats().getNutrition() < player.getFoodStats().getNutritionLimit() && PlayerTimers.canSlowFeed(player))
		{
			world.playSoundAtEntity(player, "projecte:item.peheal", 1.0F, 1.0F);
			moze_intel.projecte.compat.PECompatHelper.feedPlayer(player);
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
		if (!world.isRemote && ProjectEConfig.bodyPedCooldown != -1)
		{
			DMPedestalTile tile = ((DMPedestalTile) world.getBlockTileEntity(x, y, z));
			if (tile.getActivityCooldown() == 0)
			{
				List<ServerPlayer> players = world.getEntitiesWithinAABB(ServerPlayer.class, tile.getEffectBounds());

				for (ServerPlayer player : players)
				{
					if (player.getFoodStats().getNutrition() < player.getFoodStats().getNutritionLimit())
					{
						world.playSoundAtEntity(player, "projecte:item.peheal", 1.0F, 1.0F);
						moze_intel.projecte.compat.PECompatHelper.feedPlayer(player); // 1/2 shank
					}
				}

				tile.setActivityCooldown(ProjectEConfig.bodyPedCooldown);
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
		if (ProjectEConfig.bodyPedCooldown != -1)
		{
			list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.body.pedestal1"));
			list.add(EnumChatFormatting.BLUE + String.format(
					StatCollector.translateToLocal("pe.body.pedestal2"), MathUtils.tickToSecFormatted(ProjectEConfig.bodyPedCooldown)));
		}
		return list;
	}

	@Override
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List list, boolean par4, net.minecraft.Slot slot)
	{
		super.addInformation(stack, player, list, par4, slot);
		list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.body.tooltip1"));
	}
}
