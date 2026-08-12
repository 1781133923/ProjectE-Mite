package moze_intel.projecte.gameObjs.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.api.item.IProjectileShooter;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.entity.EntityLavaProjectile;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.FluidHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.IconRegister;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.ItemStack;
import net.minecraft.TileEntity;
import net.minecraft.EnumChatFormatting;
import net.minecraft.MovingObjectPosition;
import net.minecraft.StatCollector;
import net.minecraft.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class VolcaniteAmulet extends ItemPE implements IProjectileShooter, IBauble, IPedestalItem, IFireProtector
{
	public VolcaniteAmulet()
	{
		this.setUnlocalizedName("volcanite_amulet");
		this.setMaxStackSize(1);
		this.setContainerItem(this);
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int sideHit, float f1, float f2, float f3)
	{
		if (!world.isRemote && PlayerHelper.hasEditPermission(((ServerPlayer) player), x, y, z))
		{
			TileEntity tile = world.getBlockTileEntity(x, y, z);

			if (tile instanceof IFluidHandler)
			{
				IFluidHandler tank = (IFluidHandler) tile;

				if (FluidHelper.canFillTank(tank, FluidRegistry.LAVA, sideHit))
				{
					if (consumeFuel(player, stack, 32.0F, true))
					{
						FluidHelper.fillTank(tank, FluidRegistry.LAVA, sideHit, 1000);
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if (!world.isRemote)
		{
			MovingObjectPosition mop = moze_intel.projecte.compat.PECompatHelper.getMovingObjectPositionFromPlayer(world, player);
			if (mop != null && mop.getEntityHit() == null)
			{
				int i = mop.blockX;
				int j = mop.blockY;
				int k = mop.blockZ;
				if (!(world.getBlockTileEntity(i, j, k) instanceof IFluidHandler))
				{
					switch(mop.sideHit) // Ripped from vanilla ItemBucket and simplified
					{
						case 0: --j; break;
						case 1: ++j; break;
						case 2: --k; break;
						case 3: ++k; break;
						case 4: --i; break;
						case 5: ++i; break;
						default: break;
					}

					if (world.isAirBlock(i, j, k) && consumeFuel(player, stack, 32, true))
					{
						placeLava(world, player, i, j, k);
						world.playSoundAtEntity(player, "projecte:item.petransmute", 1.0F, 1.0F);
						PlayerHelper.swingItem(player);
					}
				}
			}
		}

		return true;
	}


	private void placeLava(World world, EntityPlayer player, int i, int j, int k)
	{
		PlayerHelper.checkedPlaceBlock(((ServerPlayer) player), i, j, k, Blocks.flowing_lava, 0);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int invSlot, boolean par5)
	{
		if (!(entity instanceof EntityPlayer)) return;
		
		EntityPlayer player = (EntityPlayer) entity;

		int x = (int) Math.floor(player.posX);
		int y = (int) (player.posY - player.getYOffset());
		int z = (int) Math.floor(player.posZ);
		
		if ((world.getBlock(x, y - 1, z) == Blocks.lava || world.getBlock(x, y - 1, z) == Blocks.flowing_lava) && world.getBlock(x, y, z) == Blocks.air)
		{
			if (!player.isSneaking())
			{
				player.motionY = 0.0D;
				player.fallDistance = 0.0F;
				player.onGround = true;
			}
				
			if (!world.isRemote && player.capabilities.getWalkSpeed() < 0.25F)
			{
				PlayerHelper.setPlayerWalkSpeed(player, 0.25F);
			}
		}
		else if (!world.isRemote)
		{
			if (player.capabilities.getWalkSpeed() != Constants.PLAYER_WALK_SPEED)
			{
				PlayerHelper.setPlayerWalkSpeed(player, Constants.PLAYER_WALK_SPEED);
			}
		}
	}
	
	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		return false;
	}
	
	@Override
	public boolean shootProjectile(EntityPlayer player, ItemStack stack) 
	{
		player.worldObj.playSoundAtEntity(player, "projecte:item.petransmute", 1.0F, 1.0F);
		player.worldObj.spawnEntityInWorld(new EntityLavaProjectile(player.worldObj, player));
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		this.itemIcon = register.registerIcon(this.getTexture("rings", "volcanite_amulet"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4, net.minecraft.Slot slot)
	{
		list.add(String.format(StatCollector.translateToLocal("pe.volcanite.tooltip1"), ClientKeyHelper.getKeyName(PEKeybind.FIRE_PROJECTILE)));
		list.add(StatCollector.translateToLocal("pe.volcanite.tooltip2"));
		list.add(StatCollector.translateToLocal("pe.volcanite.tooltip3"));
		list.add(StatCollector.translateToLocal("pe.volcanite.tooltip4"));
	}
	
	@Override
	@Optional.Method(modid = "Baubles")
	public baubles.api.BaubleType getBaubleType(ItemStack itemstack)
	{
		return BaubleType.AMULET;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onWornTick(ItemStack stack, EntityLivingBase ent) 
	{
		if (!(ent instanceof EntityPlayer)) 
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer) ent;
		World world = player.worldObj;

		int x = (int) Math.floor(player.posX);
		int y = (int) (player.posY - player.getYOffset());
		int z = (int) Math.floor(player.posZ);
		
		if ((world.getBlock(x, y - 1, z) == Blocks.lava || world.getBlock(x, y - 1, z) == Blocks.flowing_lava) && world.getBlock(x, y, z) == Blocks.air)
		{
			if (!player.isSneaking())
			{
				player.motionY = 0.0D;
				player.fallDistance = 0.0F;
				player.onGround = true;
			}
				
			if (!world.isRemote && player.capabilities.getWalkSpeed() < 0.25F)
			{
				PlayerHelper.setPlayerWalkSpeed(player, 0.25F);
			}
		}
		else if (!world.isRemote)
		{
			if (player.capabilities.getWalkSpeed() != Constants.PLAYER_WALK_SPEED)
			{
				PlayerHelper.setPlayerWalkSpeed(player, Constants.PLAYER_WALK_SPEED);
			}
		}
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
		if (!world.isRemote && ProjectEConfig.volcanitePedCooldown != -1)
		{
			DMPedestalTile tile = ((DMPedestalTile) world.getBlockTileEntity(x, y, z));
			if (tile.getActivityCooldown() == 0)
			{
				moze_intel.projecte.compat.PECompatHelper.setRainTime(world, 0);
				moze_intel.projecte.compat.PECompatHelper.setThunderTime(world, 0);
				moze_intel.projecte.compat.PECompatHelper.setRaining(world, false);
				moze_intel.projecte.compat.PECompatHelper.setThundering(world, false);

				tile.setActivityCooldown(ProjectEConfig.volcanitePedCooldown);
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
		if (ProjectEConfig.volcanitePedCooldown != -1)
		{
			list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.volcanite.pedestal1"));
			list.add(EnumChatFormatting.BLUE + String.format(StatCollector.translateToLocal("pe.volcanite.pedestal2"), MathUtils.tickToSecFormatted(ProjectEConfig.volcanitePedCooldown)));
		}
		return list;
	}

	@Override
	public boolean canProtectAgainstFire(ItemStack stack, net.minecraft.EntityPlayer player)
	{
		return true;
	}
}
