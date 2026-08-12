package moze_intel.projecte.gameObjs.items.rings;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.Optional;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.Block;
import net.minecraft.BlockCrops;
import net.minecraft.BlockCropsDead;
import net.minecraft.BlockStem;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.EnumChatFormatting;
import net.minecraft.StatCollector;
import net.minecraft.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class HarvestGoddess extends RingToggle implements IPedestalItem, IBauble
{
	private static final Map<Integer, Block> SEED_TO_PLANT = new HashMap<>();
	private static boolean seedMapBuilt;

	public HarvestGoddess()
	{
		super("harvest_god");
		
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

		// While carried, the ring does NOT accelerate growth (that is the
		// pedestal function). It cures blighted (sick) crops and harvests the
		// mature ones: drops go into nearby chests and the crops are replanted.
		WorldHelper.cureBlightedCrops(world, player.posX, player.posY, player.posZ);
		WorldHelper.harvestNearbyMatureCrops(world, player.posX, player.posY, player.posZ);
	}
	
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (world.isRemote || !player.canPlayerEdit(x, y, z, stack))
		{
			return false;
		}
		
		if (player.isSneaking())
		{
			Object[] obj = getStackFromInventory(player.inventory.mainInventory, Items.dye, 15, 4);

			if (obj == null) 
			{
				return false;
			}
			
			ItemStack boneMeal = (ItemStack) obj[1];

			if (boneMeal != null && useBoneMeal(world, x, y, z))
			{
				player.inventory.decrStackSize((Integer) obj[0], 4);
				player.inventoryContainer.detectAndSendChanges();
				return true;
			}
			
			return false;
		}
		
		return plantSeeds(world, player, x, y, z);
	}
	
	private boolean useBoneMeal(World world, int xCoord, int yCoord, int zCoord)
	{
		boolean result = false;
		
		for (int x = xCoord - 15; x <= xCoord + 15; x++)
			for (int z = zCoord - 15; z <= zCoord + 15; z++)
			{
				Block crop = world.getBlock(x, yCoord, z);
				
				if (crop == null)
				{
					continue;
				}

				// MITE: bonemeal cures blight on crops, grows stems, and does
				// not instantly mature wheat/carrot/potato/onion. The Goddess
				// ring also nudges ordinary crops forward one growth stage.
				if (crop instanceof BlockCrops)
				{
					BlockCrops crops = (BlockCrops) crop;
					int meta = world.getBlockMetadata(x, yCoord, z);
					
					if (crops.isBlighted(meta))
					{
						crops.setBlighted(world, x, yCoord, z, false);
						result = true;
					}
					else if (!crops.isDead() && !crops.isMature(meta)
							&& crops.isLightLevelSuitableForGrowth(world.getBlockLightValue(x, yCoord + 1, z)))
					{
						int newMeta = crops.incrementGrowth(meta);
						if (newMeta != meta)
						{
							world.setBlockMetadataWithNotify(x, yCoord, z, newMeta, 2);
							result = true;
						}
					}
				}
				else if (crop instanceof BlockStem)
				{
					if (((BlockStem) crop).fertilizeStem(world, x, yCoord, z, new ItemStack(Items.dye, 1, 15)))
					{
						result = true;
					}
				}
			}
		
		return result;
	}
	
	private boolean plantSeeds(World world, EntityPlayer player, int xCoord, int yCoord, int zCoord)
	{
		boolean result = false;
		
		List<StackWithSlot> seeds = getAllSeeds(player.inventory.mainInventory);
		
		if (seeds.isEmpty())
		{
			return false;
		}
		
		for (int x = xCoord - 8; x <= xCoord + 8; x++)
			for (int z = zCoord - 8; z <= zCoord + 8; z++)
			{
				Block block = world.getBlock(x, yCoord, z);
				
				if (block == null || block == Blocks.air) 
				{
					continue;
				}
				
				for (int i = 0; i < seeds.size(); i++)
				{
					StackWithSlot s = seeds.get(i);
					Block plantBlock = SEED_TO_PLANT.get(s.stack.itemID);
					
					if (plantBlock == null)
					{
						continue;
					}
					
				if ((block == net.minecraft.Block.tilledField || block == net.minecraft.Block.dirt || block == net.minecraft.Block.grass)
						&& world.isAirBlock(x, yCoord + 1, z)
						&& plantBlock.isLegalAt(world, x, yCoord + 1, z, 0))
				{
					world.setBlock(x, yCoord + 1, z, plantBlock == null ? 0 : plantBlock.blockID);
						player.inventory.decrStackSize(s.slot, 1);
						player.inventoryContainer.detectAndSendChanges();
						
						s.stack.stackSize--;
						
						if (s.stack.stackSize <= 0)
						{
							seeds.remove(i);
						}
						
						if (!result)
						{
							result = true;
						}
					}
				}
			}
		
		return result;
	}
	
	private List<StackWithSlot> getAllSeeds(ItemStack[] inv) 
	{
		List<StackWithSlot> result = Lists.newArrayList();

		buildSeedMapIfNeeded();
		
		for (int i = 0; i < inv.length; i++)
		{
			ItemStack stack = inv[i];
			
			if (stack != null)
			{
				if (SEED_TO_PLANT.containsKey(stack.itemID))
				{
					result.add(new StackWithSlot(stack, i));
				}
			}
		}
		
		return result;
	}

	/**
	 * Builds the seed item id -> plant block map from MITE's own crop/stem
	 * blocks (MITE does not expose a public seed->plant API). Built lazily so
	 * the lookup can never run before MITE's blocks are registered.
	 */
	private static void buildSeedMapIfNeeded()
	{
		if (seedMapBuilt)
		{
			return;
		}
		for (Block block : Block.blocksList)
		{
			if (block == null)
			{
				continue;
			}
			if (block instanceof BlockCrops && !(block instanceof BlockCropsDead))
			{
				SEED_TO_PLANT.put(net.minecraft.PEPlantCompat.getCropSeedItem((BlockCrops) block), block);
			}
			else if (block instanceof BlockStem)
			{
				SEED_TO_PLANT.put(((BlockStem) block).getSeedItem(), block);
			}
		}
		seedMapBuilt = true;
	}
	
	private Object[] getStackFromInventory(ItemStack[] inv, Item item, int meta, int minAmount)
	{
		Object[] obj = new Object[2];
		
		for (int i = 0; i < inv.length;i++)
		{
			ItemStack stack = inv[i];
			
			if (stack != null && stack.stackSize >= minAmount && stack.getItem() == item && stack.getItemSubtype() == meta)
			{
				obj[0] = i;
				obj[1] = stack;
				return obj;
			}
		}
		
		return null;
	}

		
	@Override
	public void changeMode(EntityPlayer player, ItemStack stack)
	{
		if (getMode(stack) == 0)
		{
			if (getEmc(stack) == 0 && !consumeFuel(player, stack, 64, true))
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

	@Override
	public void updateInPedestal(World world, int x, int y, int z)
	{
		if (!world.isRemote && ProjectEConfig.harvestPedCooldown != -1)
		{
			DMPedestalTile tile = (DMPedestalTile) world.getBlockTileEntity(x, y, z);
			if (tile.getActivityCooldown() == 0)
			{
				WorldHelper.growNearbyRandomly(true, world, x, y, z, null);
				tile.setActivityCooldown(ProjectEConfig.harvestPedCooldown);
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
		if (ProjectEConfig.harvestPedCooldown != -1)
		{
			list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.harvestgod.pedestal1"));
			list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.harvestgod.pedestal2"));
			list.add(EnumChatFormatting.BLUE + String.format(
					StatCollector.translateToLocal("pe.harvestgod.pedestal3"), MathUtils.tickToSecFormatted(ProjectEConfig.harvestPedCooldown)));
		}
		return list;
	}

	@Override
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List list, boolean par4, net.minecraft.Slot slot)
	{
		super.addInformation(stack, player, list, par4, slot);
		list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.harvestgod.tooltip1"));
	}

	private class StackWithSlot
	{
		public final int slot;
		public final ItemStack stack;
		
		public StackWithSlot(ItemStack stack, int slot) 
		{
			this.stack = stack.copy();
			this.slot = slot;
		}
	}
}
