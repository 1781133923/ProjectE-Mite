package moze_intel.projecte.gameObjs.items.rings;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.google.common.collect.Lists;
import moze_intel.projecte.api.item.IAlchBagItem;
import moze_intel.projecte.api.item.IAlchChestItem;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.gameObjs.entity.EntityLootBall;
import moze_intel.projecte.gameObjs.tiles.AlchChestTile;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.WorldHelper;

import java.util.List;

import cpw.mods.fml.common.Optional;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityItem;
import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.ItemStack;
import net.minecraft.TileEntity;
import net.minecraft.AxisAlignedBB;
import net.minecraft.EnumChatFormatting;
import net.minecraft.StatCollector;
import net.minecraft.World;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class BlackHoleBand extends RingToggle implements IAlchBagItem, IAlchChestItem, IBauble, IPedestalItem
{
	public BlackHoleBand()
	{
		super("black_hole");
		
	}
	
	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if (!world.isRemote)
		{
			changeMode(player, stack);
		}
		
		return true;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) 
	{
		if (getMode(stack) != 1 || !(entity instanceof EntityPlayer) || par4 > 8)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer) entity;
		AxisAlignedBB bBox = player.boundingBox.expand(7, 7, 7);
		List<EntityItem> itemList = world.getEntitiesWithinAABB(EntityItem.class, bBox);
		
		for (EntityItem item : itemList)
		{
			if (ItemHelper.hasSpace(player.inventory.mainInventory, item.getEntityItem()))
			{
				WorldHelper.gravitateEntityTowards(item, player.posX, player.posY, player.posZ);
			}
		}
		
		List<EntityLootBall> ballList = world.getEntitiesWithinAABB(EntityLootBall.class, bBox);
		
		for (EntityLootBall ball : ballList)
		{
			WorldHelper.gravitateEntityTowards(ball, player.posX, player.posY, player.posZ);
		}
	}

	@Override
	public void updateInPedestal(World world, int x, int y, int z)
	{
		DMPedestalTile tile = ((DMPedestalTile) world.getBlockTileEntity(x, y, z));
		if (tile != null)
		{
			List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, tile.getEffectBounds());
			for (EntityItem item : list)
			{
				WorldHelper.gravitateEntityTowards(item, x + 0.5, y + 0.5, z + 0.5);
				if (!world.isRemote && item.getDistanceSq(x + 0.5, y + 0.5, z + 0.5) < 1.21 && !item.isDead)
				{
					suckDumpItem(item, tile);
				}
			}
		}
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public BaubleType getBaubleType(ItemStack itemstack)
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

	private void suckDumpItem(EntityItem item, DMPedestalTile tile)
	{
		List<TileEntity> list = WorldHelper.getAdjacentTileEntities(tile.getWorldObj(), tile);
		for (TileEntity tileEntity : list)
		{
			if (tileEntity instanceof IInventory)
			{
				IInventory inv = ((IInventory) tileEntity);
				ItemStack result = ItemHelper.pushStackInInv(inv, item.getEntityItem());
				if (result != null)
				{
					item.setEntityItemStack(result);
				}
				else
				{
					item.setDead();
					break;
				}
			}
		}
	}

	@Override
	public List<String> getPedestalDescription()
	{
		return Lists.newArrayList(
				EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.bhb.pedestal1"),
				EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.bhb.pedestal2")
		);
	}

	@Override
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List list, boolean par4, net.minecraft.Slot slot)
	{
		super.addInformation(stack, player, list, par4, slot);
		list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.bhb.tooltip1"));
	}

	@Override
	public void updateInAlchChest(World world, int x, int y, int z, ItemStack stack)
	{
		AlchChestTile tile = ((AlchChestTile) world.getBlockTileEntity(x, y, z));
		if (getMode(stack) == 1)
		{
			AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(tile.xCoord - 5, tile.yCoord - 5, tile.zCoord - 5, tile.xCoord + 5, tile.yCoord + 5, tile.zCoord + 5);
			double centeredX = tile.xCoord + 0.5;
			double centeredY = tile.yCoord + 0.5;
			double centeredZ = tile.zCoord + 0.5;

			for (EntityItem e : (List<EntityItem>) tile.getWorldObj().getEntitiesWithinAABB(EntityItem.class, aabb))
			{
				WorldHelper.gravitateEntityTowards(e, centeredX, centeredY, centeredZ);
				if (!e.worldObj.isRemote && !e.isDead && e.getDistanceSq(centeredX, centeredY, centeredZ) < 1.21)
				{
					ItemStack result = ItemHelper.pushStackInInv(tile, e.getEntityItem());
					if (result != null)
					{
						e.setEntityItemStack(result);
					}
					else
					{
						e.setDead();
					}
				}
			}

			for (EntityLootBall e : (List<EntityLootBall>) tile.getWorldObj().getEntitiesWithinAABB(EntityLootBall.class, aabb))
			{
				WorldHelper.gravitateEntityTowards(e, centeredX, centeredY, centeredZ);
				if (!e.worldObj.isRemote && !e.isDead && e.getDistanceSq(centeredX, centeredY, centeredZ) < 1.21)
				{
					ItemHelper.pushLootBallInInv(tile, e);
				}
			}
		}
	}

	@Override
	public boolean updateInAlchBag(ItemStack[] inv, EntityPlayer player, ItemStack stack)
	{
		if (getMode(stack) == 1)
		{

			for (EntityItem e : (List<EntityItem>) player.worldObj.getEntitiesWithinAABB(EntityItem.class, player.boundingBox.expand(5, 5, 5)))
			{
				WorldHelper.gravitateEntityTowards(e, player.posX, player.posY, player.posZ);
			}

			for (EntityLootBall e : (List<EntityLootBall>) player.worldObj.getEntitiesWithinAABB(EntityLootBall.class, player.boundingBox.expand(5, 5, 5)))
			{
				WorldHelper.gravitateEntityTowards(e, player.posX, player.posY, player.posZ);
			}
		}
		return false;
	}
}
