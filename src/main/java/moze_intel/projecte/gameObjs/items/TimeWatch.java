package moze_intel.projecte.gameObjs.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.api.item.IModeChanger;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.Block;
import net.minecraft.BlockFluid;
import net.minecraft.IGrowable;
import net.minecraft.IconRegister;
import net.minecraft.Entity;
import net.minecraft.EntityLiving;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.TileEntity;
import net.minecraft.AxisAlignedBB;
import net.minecraft.ChatComponentTranslation;
import net.minecraft.EnumChatFormatting;
import net.minecraft.Icon;
import net.minecraft.MathHelper;
import net.minecraft.StatCollector;
import net.minecraft.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.BlockFluidBase;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class TimeWatch extends ItemCharge implements IModeChanger, IBauble, IPedestalItem
{
	private static Set<String> internalBlacklist = Sets.newHashSet(
			"moze_intel.projecte.gameObjs.tiles.DMPedestalTile",
			"Reika.ChromatiCraft.TileEntity.AOE.TileEntityAccelerator",
			"com.sci.torcherino.tile.TileTorcherino",
			"com.sci.torcherino.tile.TileCompressedTorcherino"
	);

	@SideOnly(Side.CLIENT)
	private Icon ringOff;
	@SideOnly(Side.CLIENT)
	private Icon ringOn;
	
	public TimeWatch() 
	{
		super("time_watch", (byte)2);
		
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){
		// 右键切换时间加速/回溯模式。
		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if (!world.isRemote)
		{
			if (!ProjectEConfig.enableTimeWatch)
			{
				moze_intel.projecte.compat.PEChatHelper.send(player, new ChatComponentTranslation("pe.timewatch.disabled"));
				return true;
			}

			if (!stack.hasTagCompound())
			{
				stack.stackTagCompound = new NBTTagCompound();
			}

			byte current = getTimeBoost(stack);

			setTimeBoost(stack, (byte) (current == 2 ? 0 : current + 1));

			moze_intel.projecte.compat.PEChatHelper.send(player, new ChatComponentTranslation("pe.timewatch.mode_switch", new ChatComponentTranslation(getTimeName(stack)).getUnformattedTextForChat()));
		}

		return true;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int invSlot, boolean isHeld) 
	{
		// 手持/穿戴时的时间加速、时间回溯以及附近的方块/生物加速效果。
		if (!stack.hasTagCompound())
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		if (!(entity instanceof EntityPlayer))
		{
			return;
		}

		if (!ProjectEConfig.enableTimeWatch)
		{
			return;
		}

		byte timeControl = getTimeBoost(stack);

		if (world.getWorldInfo() != null)
		{
			// Same time-adjustment method ITE uses (first_day_longer_day_time):
			// World.getTotalWorldTime()/setTotalWorldTime(long). Do NOT gate on the
			// doDaylightCycle game rule - MITE may have it off, which silently
			// disabled the whole time control.
			long totalTime = world.getTotalWorldTime();
			// Original ProjectE speed: (charge+1)*4 ticks per tick.
			int delta = (getCharge(stack) + 1) * 4;
			if (timeControl == 1)
			{
				world.setTotalWorldTime(Math.min(totalTime + delta, Long.MAX_VALUE));
			}
			else if (timeControl == 2)
			{
				world.setTotalWorldTime(Math.max(totalTime - delta, 0));
			}
		}

		if (world.isRemote || getMode(stack) == 0)
		{
			return;
		}

		EntityPlayer player = (EntityPlayer) entity;
		double reqEmc = getEmcPerTick(this.getCharge(stack));
		
		if (!consumeFuel(player, stack, reqEmc, true))
		{
			return;
		}
		
		int charge = this.getCharge(stack);
		int bonusTicks = 0;
		float mobSlowdown = 0;
		
		if (charge == 0)
		{
			bonusTicks = 8;
			mobSlowdown = 0.25F;
		}
		else if (charge == 1)
		{
			bonusTicks = 12;
			mobSlowdown = 0.16F;
		}
		else
		{
			bonusTicks = 16;
			mobSlowdown = 0.12F;
		}
			
		AxisAlignedBB bBox = player.boundingBox.expand(8, 8, 8);

		speedUpTileEntities(world, bonusTicks, bBox);
		speedUpRandomTicks(world, bonusTicks, bBox);
		slowMobs(world, bBox, mobSlowdown);
	}

	private void slowMobs(World world, AxisAlignedBB bBox, float mobSlowdown)
	{
		if (bBox == null) // Sanity check for chunk unload weirdness
		{
			return;
		}
		for (Object obj : world.getEntitiesWithinAABB(EntityLiving.class, bBox))
		{
			Entity ent = (Entity) obj;

			if (ent.motionX != 0)
			{
				ent.motionX *= mobSlowdown;
			}

			if (ent.motionZ != 0)
			{
				ent.motionZ *= mobSlowdown;
			}
		}
	}

	private void speedUpTileEntities(World world, int bonusTicks, AxisAlignedBB bBox)
	{
		if (bBox == null || bonusTicks == 0) // Sanity check the box for chunk unload weirdness
		{
			return;
		}
		List<TileEntity> list = WorldHelper.getTileEntitiesWithinAABB(world, bBox);
		for (int i = 0; i < bonusTicks; i++)
		{
			for (TileEntity tile : list)
			{
				if (!tile.isInvalid() && !internalBlacklist.contains(tile.getClass().getName()))
				{
					tile.updateEntity();
				}
			}
		}
	}

	private void speedUpRandomTicks(World world, int bonusTicks, AxisAlignedBB bBox)
	{
		if (bBox == null || bonusTicks == 0) // Sanity check the box for chunk unload weirdness
		{
			return;
		}
		for (int x = (int) bBox.minX; x <= bBox.maxX; x++)
		{
			for (int y = (int) bBox.minY; y <= bBox.maxY; y++)
			{
				for (int z = (int) bBox.minZ; z <= bBox.maxZ; z++)
				{
					Block block = world.getBlock(x, y, z);

					// MITE represents air as a null block (1.7.10 had a non-null air block).
					if (block == null)
					{
						continue;
					}

					if (block.getTickRandomly()
			&& !(block instanceof BlockFluid) // Don't speed vanilla non-source blocks - dupe issues
							&& !(block instanceof BlockFluidBase) // Don't speed Forge fluids - just in case of dupes as well
							&& !(block instanceof IGrowable)
							&& !(block instanceof IPlantable) // All plants should be sped using Harvest Goddess
						)
					{
						for (int i = 0; i < bonusTicks; i++)
						{
							block.updateTick(world, x, y, z, itemRand);
						}
					}
				}
			}

		}
	}

	private String getTimeName(ItemStack stack)
	{
		byte mode = getTimeBoost(stack);
		switch (mode)
		{
			case 0:
				return "pe.timewatch.off";
			case 1:
				return "pe.timewatch.ff";
			case 2:
				return "pe.timewatch.rw";
			default:
				return "ERROR_INVALID_MODE";
		}
	}

	private byte getTimeBoost(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new net.minecraft.NBTTagCompound();
		}
		return stack.stackTagCompound.getByte("TimeMode");
	}

	private void setTimeBoost(ItemStack stack, byte time)
	{
		stack.stackTagCompound.setByte("TimeMode", (byte) MathHelper.clamp_int(time, 0, 2));
	}

	public double getEmcPerTick(int charge)
	{
		int actualCharge = charge + 1;
		return (10.0D * actualCharge) / 20.0D;
	}

	@Override
	public byte getMode(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new NBTTagCompound();
		}
		return (byte) (stack.stackTagCompound.getBoolean("On") ? 1 : 0);
	}

	@Override
	public void changeMode(EntityPlayer player, ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new NBTTagCompound();
		}
		if (getMode(stack) == 0)
		{
			stack.stackTagCompound.setBoolean("On", true);
			playChargeSound(player);
		}
		else 
		{
			stack.stackTagCompound.setBoolean("On", false);
			playUnChargeSound(player);
		}
	}
	
	public void playChargeSound(EntityPlayer player)
	{
		player.worldObj.playSoundAtEntity(player, "projecte:item.tock", 0.8F, 1.25F);
	}
	
	public void playUnChargeSound(EntityPlayer player)
	{
		player.worldObj.playSoundAtEntity(player, "projecte:item.tock", 0.8F, 0.85F);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIconFromSubtype(int dmg)
	{
		if (dmg == 0)
		{
			return ringOff;
		}
		
		return ringOn;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		ringOff = register.registerIcon(this.getTexture("rings", "time_watch_off"));
		ringOn = register.registerIcon(this.getTexture("rings", "time_watch_on"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool, net.minecraft.Slot slot)
	{
		list.add(StatCollector.translateToLocal("pe.timewatch.tooltip1"));
		list.add(StatCollector.translateToLocal("pe.timewatch.tooltip2"));

		if (stack.hasTagCompound())
		{
			list.add(String.format(StatCollector.translateToLocal("pe.timewatch.mode"),
					StatCollector.translateToLocal(getTimeName(stack))));
		}
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public baubles.api.BaubleType getBaubleType(ItemStack itemstack)
	{
		return BaubleType.BELT;
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
		/* Change from old EE2 behaviour (universally increased tickrate) for safety and impl reasons.
		Now the same as activated watch in hand but more powerful.
		Can be changed at sinkillerj's discretion. */

		if (!world.isRemote && ProjectEConfig.enableTimeWatch)
		{
			AxisAlignedBB bBox = ((DMPedestalTile) world.getBlockTileEntity(x, y, z)).getEffectBounds();
			if (ProjectEConfig.timePedBonus > 0) {
				speedUpTileEntities(world, ProjectEConfig.timePedBonus, bBox);
				speedUpRandomTicks(world, ProjectEConfig.timePedBonus, bBox);
			}

			if (ProjectEConfig.timePedMobSlowness < 1.0F) {
				slowMobs(world, bBox, ProjectEConfig.timePedMobSlowness);
			}
		}
	}

	@Override
	public List<String> getPedestalDescription()
	{
		List<String> list = Lists.newArrayList();
		if (ProjectEConfig.timePedBonus > 0) {
			list.add(EnumChatFormatting.BLUE +
				String.format(StatCollector.translateToLocal("pe.timewatch.pedestal1"), ProjectEConfig.timePedBonus));
		}
		if (ProjectEConfig.timePedMobSlowness < 1.0F)
		{
			list.add(EnumChatFormatting.BLUE +
					String.format(StatCollector.translateToLocal("pe.timewatch.pedestal2"), ProjectEConfig.timePedMobSlowness));
		}
		if (ProjectEConfig.enableTimeWatch)
		{
			list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.timewatch.pedestal3"));
		}
		return list;
	}

	public static void blacklist(Class<? extends TileEntity> clazz)
	{
		internalBlacklist.add(clazz.getName());
	}
}
