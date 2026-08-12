package moze_intel.projecte.gameObjs.items.rings;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.Optional;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.api.item.IProjectileShooter;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.entity.EntityFireProjectile;
import moze_intel.projecte.gameObjs.items.IFireProtector;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.BlockTNT;
import net.minecraft.Entity;
import net.minecraft.EntityLiving;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.ItemStack;
import net.minecraft.DamageSource;
import net.minecraft.EnumChatFormatting;
import net.minecraft.MovingObjectPosition;
import net.minecraft.StatCollector;
import net.minecraft.World;

import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class Ignition extends RingToggle implements IBauble, IPedestalItem, IFireProtector, IProjectileShooter
{
	public Ignition()
	{
		super("ignition");
		
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int inventorySlot, boolean par5) 
	{
		if (world.isRemote || !(entity instanceof EntityPlayer)) return;
		
		super.onUpdate(stack, world, entity, inventorySlot, par5);
		ServerPlayer player = (ServerPlayer)entity;

		if (getMode(stack) != 0)
		{
			if (getEmc(stack) == 0 && !consumeFuel(player, stack, 64, false))
			{
				setMode(stack, 0);
			}
			else 
			{
				WorldHelper.igniteNearby(world, player);
				removeEmc(stack, 0.32F);
			}
		}
		else 
		{
			WorldHelper.extinguishNearby(world, player);
		}
	}
	
	@Override
	public void changeMode(EntityPlayer player, ItemStack stack)
	{
		
		if (getMode(stack) == 0)
		{
			if (getEmc(stack) == 0 && !consumeFuel(player, stack, 64, false))
			{
				//NOOP (used to be sounds)
			}
			else
			{
				setMode(stack, 1);
			}
		}
		else
		{
			setMode(stack, 0);
		}
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if (!world.isRemote)
		{
			boolean ignitedTnt = false;
			MovingObjectPosition mop = moze_intel.projecte.compat.PECompatHelper.getMovingObjectPositionFromPlayer(world, player);
			if (mop != null && mop.getEntityHit() == null)
			{
				if (world.getBlock(mop.blockX, mop.blockY, mop.blockZ) instanceof BlockTNT
						&& PlayerHelper.hasBreakPermission(((ServerPlayer) player), mop.blockX, mop.blockY, mop.blockZ))
				{
					// Ignite TNT or derivatives
					net.minecraft.BlockTNT.ignite(world, mop.blockX, mop.blockY, mop.blockZ, player);
					world.setBlockToAir(mop.blockX, mop.blockY, mop.blockZ);
					ignitedTnt = true;
				}
			}
			if (!ignitedTnt)
			{
				// The ring's other right-click ability: launch a fire projectile.
				ServerPlayer serverPlayer = (ServerPlayer) player;
				if (moze_intel.projecte.handlers.PlayerChecks.getProjectileCooldown(serverPlayer) <= 0)
				{
					if (shootProjectile(player, stack))
					{
						PlayerHelper.swingItem(player);
					}
					moze_intel.projecte.handlers.PlayerChecks.resetProjectileCooldown(serverPlayer);
				}
			}
			world.playSoundAtEntity(player, "projecte:item.pepower", 1.0F, 1.0F);
		}
		return true;
	}

	@Override
	public void updateInPedestal(World world, int x, int y, int z)
	{
		if (!world.isRemote && ProjectEConfig.ignitePedCooldown != -1)
		{
			DMPedestalTile tile = ((DMPedestalTile) world.getBlockTileEntity(x, y, z));
			if (tile.getActivityCooldown() == 0)
			{
				List<EntityLiving> list = world.getEntitiesWithinAABB(EntityLiving.class, tile.getEffectBounds());
				for (EntityLiving living : list)
				{
					living.attackEntityFrom(new net.minecraft.Damage(DamageSource.inFire, 3.0f));
					living.setFire(8);
				}

				tile.setActivityCooldown(ProjectEConfig.ignitePedCooldown);
			}
			else
			{
				tile.decrementActivityCooldown();
			}
		}
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public baubles.api.BaubleType getBaubleType(ItemStack itemstack)
	{
		return BaubleType.RING;
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
	public List<String> getPedestalDescription()
	{
		List<String> list = Lists.newArrayList();
		if (ProjectEConfig.ignitePedCooldown != -1)
		{
			list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.ignition.pedestal1"));
			list.add(EnumChatFormatting.BLUE + String.format(
					StatCollector.translateToLocal("pe.ignition.pedestal2"), MathUtils.tickToSecFormatted(ProjectEConfig.ignitePedCooldown)));
		}
		return list;
	}
	
	@Override
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List list, boolean par4, net.minecraft.Slot slot)
	{
		super.addInformation(stack, player, list, par4, slot);
		list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.ignition.tooltip1"));
	}

	@Override
	public boolean shootProjectile(EntityPlayer player, ItemStack stack)
	{
		World world = player.worldObj;
		
		if(world.isRemote) return false;
		
		EntityFireProjectile fire = new EntityFireProjectile(world, player);
		world.spawnEntityInWorld(fire);
		
		return true;
	}

	@Override
	public boolean canProtectAgainstFire(ItemStack stack, EntityPlayer player)
	{
		return true;
	}
}
