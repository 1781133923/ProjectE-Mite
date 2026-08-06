package moze_intel.projecte.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.entity.EntityLootBall;
import moze_intel.projecte.gameObjs.tiles.AlchChestTile;
import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockCactus;
import net.minecraft.BlockCocoa;
import net.minecraft.BlockCrops;
import net.minecraft.BlockDeadBush;
import net.minecraft.BlockFlower;
import net.minecraft.BlockLilyPad;
import net.minecraft.BlockMelon;
import net.minecraft.BlockMushroom;
import net.minecraft.BlockNetherStalk;
import net.minecraft.BlockPumpkin;
import net.minecraft.BlockReed;
import net.minecraft.BlockSapling;
import net.minecraft.BlockStem;
import net.minecraft.BlockTallGrass;
import net.minecraft.BlockVine;
import net.minecraft.IGrowable;
import net.minecraft.Enchantment;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Entity;
import net.minecraft.EntityLiving;
import net.minecraft.IProjectile;
import net.minecraft.EntityItem;
import net.minecraft.EntityBlaze;
import net.minecraft.EntityCreeper;
import net.minecraft.EntityEnderman;
import net.minecraft.EntityGhast;
import net.minecraft.EntityPigZombie;
import net.minecraft.EntitySilverfish;
import net.minecraft.EntitySkeleton;
import net.minecraft.EntitySlime;
import net.minecraft.EntitySpider;
import net.minecraft.EntityWitch;
import net.minecraft.EntityZombie;
import net.minecraft.IMob;
import net.minecraft.EntityBat;
import net.minecraft.EntityChicken;
import net.minecraft.EntityCow;
import net.minecraft.EntityHorse;
import net.minecraft.EntityMooshroom;
import net.minecraft.EntityOcelot;
import net.minecraft.EntityPig;
import net.minecraft.EntitySheep;
import net.minecraft.EntitySquid;
import net.minecraft.EntityVillager;
import net.minecraft.EntityWolf;
import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.ServerPlayer;
import net.minecraft.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.TileEntity;
import net.minecraft.TileEntityChest;
import net.minecraft.AxisAlignedBB;
import net.minecraft.Vec3;
import net.minecraft.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.ExplosionEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Helper class for anything that touches a World.
 * Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class WorldHelper
{
	public static final ImmutableList<Class<? extends EntityLiving>> peacefuls = ImmutableList.<Class<? extends EntityLiving>>of(
			EntitySheep.class, EntityPig.class, EntityCow.class,
			EntityMooshroom.class, EntityChicken.class, EntityBat.class,
			EntityVillager.class, EntitySquid.class, EntityOcelot.class,
			EntityWolf.class, EntityHorse.class
	);
	public static final ImmutableList<Class<? extends EntityLiving>> mobs = ImmutableList.<Class<? extends EntityLiving>>of(
			EntityZombie.class, EntitySkeleton.class, EntityCreeper.class,
			EntitySpider.class, EntityEnderman.class, EntitySilverfish.class,
			EntityPigZombie.class, EntityGhast.class, EntityBlaze.class,
			EntitySlime.class, EntityWitch.class
	);

	public static Set<Class<? extends Entity>> interdictionBlacklist = Sets.newHashSet();

	public static Set<Class<? extends Entity>> swrgBlacklist = Sets.newHashSet();

	public static boolean blacklistInterdiction(Class<? extends Entity> clazz)
	{
		if (!interdictionBlacklist.contains(clazz))
		{
			interdictionBlacklist.add(clazz);
			return true;
		}
		return false;
	}

	public static boolean blacklistSwrg(Class<? extends Entity> clazz)
	{
		if (!interdictionBlacklist.contains(clazz))
		{
			interdictionBlacklist.add(clazz);
			return true;
		}
		return false;
	}

	public static void createLootDrop(List<ItemStack> drops, World world, double x, double y, double z)
	{
		if (drops.isEmpty())
		{
			return;
		}

		ItemHelper.compactItemList(drops);

		if (ProjectEConfig.useLootBalls)
		{
			world.spawnEntityInWorld(new EntityLootBall(world, drops, x, y, z));
		}
		else
		{
			for (ItemStack drop : drops)
			{
				spawnEntityItem(world, drop, x, y, z);
			}
		}
	}

	/**
	 * Equivalent of World.newExplosion
	 */
	public static void createNovaExplosion(World world, Entity exploder, double x, double y, double z, float power)
	{
		NovaExplosion explosion = new NovaExplosion(world, exploder, x, y, z, power);
		if (!MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Start(world, explosion)))
		{
			explosion.doExplosionA();
			explosion.doExplosionB(true);
		}
	}

	public static void extinguishNearby(World world, EntityPlayer player)
	{
		for (int x = (int) (player.posX - 1); x <= player.posX + 1; x++)
			for (int y = (int) (player.posY - 1); y <= player.posY + 1; y++)
				for (int z = (int) (player.posZ - 1); z <= player.posZ + 1; z++)
					if (world.getBlock(x, y, z) == Blocks.fire && PlayerHelper.hasBreakPermission(((ServerPlayer) player), x, y, z))
					{
						world.setBlockToAir(x, y, z);
					}
	}

	public static void freezeInBoundingBox(World world, AxisAlignedBB box, EntityPlayer player, boolean random)
	{
		for (int x = (int) box.minX; x <= box.maxX; x++)
		{
			for (int y = (int) box.minY; y <= box.maxY; y++)
			{
				for (int z = (int) box.minZ; z <= box.maxZ; z++)
				{
					Block b = world.getBlock(x, y, z);

					if (b == null)
					{
						continue; // MITE represents air as a null block
					}

					if ((b == Blocks.water || b == Blocks.flowing_water) && (!random || world.rand.nextInt(128) == 0))
					{
						if (player != null)
						{
							PlayerHelper.checkedReplaceBlock(((ServerPlayer) player), x, y, z, Blocks.ice, 0);
						}
						else
						{
							world.setBlock(x, y, z, Blocks.ice.blockID);
						}
					}
					else if (b.isSolid(0))
					{
						Block b2 = world.getBlock(x, y + 1, z);

						if (b2 == Blocks.air && (!random || world.rand.nextInt(128) == 0))
						{
							if (player != null)
							{
								PlayerHelper.checkedReplaceBlock(((ServerPlayer) player), x, y + 1, z, Blocks.snow_layer, 0);
							}
							else
							{
								world.setBlock(x, y + 1, z, Blocks.snow_layer.blockID);
							}
						}
					}
				}
			}
		}
	}
	
	public static List<TileEntity> getAdjacentTileEntities(World world, TileEntity tile)
	{
		return ImmutableList.copyOf(getAdjacentTileEntitiesMapped(world, tile).values());
	}

	public static Map<ForgeDirection, TileEntity> getAdjacentTileEntitiesMapped(final World world, final TileEntity tile)
	{
		Map<ForgeDirection, TileEntity> ret = new EnumMap<>(ForgeDirection.class);

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity candidate = world.getBlockTileEntity(tile.xCoord + dir.offsetX, tile.yCoord + dir.offsetY, tile.zCoord + dir.offsetZ);
			if (candidate != null) {
				ret.put(dir, candidate);
			}
		}

		return ret;
	}

	public static ArrayList<ItemStack> getBlockDrops(World world, EntityPlayer player, Block block, ItemStack stack, int x, int y, int z)
	{
		// MITE has no list-based drop API, so harvest the block through the
		// native BlockBreakInfo path: this drops the items at the block
		// position and respects the harvester/tool (silk touch, fortune,
		// player-only crops, ...). An empty list is returned, which makes the
		// callers' loot-ball collection a no-op while the blocks still drop
		// their loot.
		if (block == null)
		{
			return new ArrayList<ItemStack>();
		}

		BlockBreakInfo info = new BlockBreakInfo(world, x, y, z);
		if (player != null)
		{
			info.setHarvestedBy(player);
		}
		block.dropBlockAsEntityItem(info);
		return new ArrayList<ItemStack>();
	}

	/**
	 * Gets an AABB for AOE digging operations. The offset increases both the breadth and depth of the box.
	 */
	public static AxisAlignedBB getBroadDeepBox(Coordinates coords, ForgeDirection direction, int offset)
	{
		if (direction.offsetX > 0)
		{
			return AxisAlignedBB.getBoundingBox(coords.x - offset, coords.y - offset, coords.z - offset, coords.x, coords.y + offset, coords.z + offset);
		}
		else if (direction.offsetX < 0)
		{
			return AxisAlignedBB.getBoundingBox(coords.x, coords.y - offset, coords.z - offset, coords.x + offset, coords.y + offset, coords.z + offset);
		}
		else if (direction.offsetY > 0)
		{
			return AxisAlignedBB.getBoundingBox(coords.x - offset, coords.y - offset, coords.z - offset, coords.x + offset, coords.y, coords.z + offset);
		}
		else if (direction.offsetY < 0)
		{
			return AxisAlignedBB.getBoundingBox(coords.x - offset, coords.y, coords.z - offset, coords.x + offset, coords.y + offset, coords.z + offset);
		}
		else if (direction.offsetZ > 0)
		{
			return AxisAlignedBB.getBoundingBox(coords.x - offset, coords.y - offset, coords.z - offset, coords.x + offset, coords.y + offset, coords.z);
		}
		else if (direction.offsetZ < 0)
		{
			return AxisAlignedBB.getBoundingBox(coords.x - offset, coords.y - offset, coords.z, coords.x + offset, coords.y + offset, coords.z + offset);
		}
		return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
	}

	/**
	 * Returns in AABB that is always 3x3 orthogonal to the side hit, but varies in depth in the direction of the side hit
	 */
	public static AxisAlignedBB getDeepBox(Coordinates coords, ForgeDirection direction, int depth)
	{
		if (direction.offsetX != 0)
		{
			if (direction.offsetX > 0)
			{
				return AxisAlignedBB.getBoundingBox(coords.x - depth, coords.y - 1, coords.z - 1, coords.x, coords.y + 1, coords.z + 1);
			}
			else return AxisAlignedBB.getBoundingBox(coords.x, coords.y - 1, coords.z - 1, coords.x + depth, coords.y + 1, coords.z + 1);
		}
		else if (direction.offsetY != 0)
		{
			if (direction.offsetY > 0)
			{
				return AxisAlignedBB.getBoundingBox(coords.x - 1, coords.y - depth, coords.z - 1, coords.x + 1, coords.y, coords.z + 1);
			}
			else return AxisAlignedBB.getBoundingBox(coords.x - 1, coords.y, coords.z - 1, coords.x + 1, coords.y + depth, coords.z + 1);
		}
		else
		{
			if (direction.offsetZ > 0)
			{
				return AxisAlignedBB.getBoundingBox(coords.x - 1, coords.y - 1, coords.z - depth, coords.x + 1, coords.y + 1, coords.z);
			}
			else return AxisAlignedBB.getBoundingBox(coords.x - 1, coords.y - 1, coords.z, coords.x + 1, coords.y + 1, coords.z + depth);
		}
	}

	/**
	 * Gets an AABB for AOE digging operations. The charge increases only the breadth of the box.
	 * Y level remains constant. As such, a direction hit is unneeded.
	 */
	public static AxisAlignedBB getFlatYBox(Coordinates coords, int offset)
	{
		return AxisAlignedBB.getBoundingBox(coords.x - offset, coords.y, coords.z - offset, coords.x + offset, coords.y, coords.z + offset);
	}
	/**
	 * A one-block-thick plane oriented to the face that was hit: a wall face
	 * digs the vertical XY/ZY plane, the floor/ceiling digs the horizontal XZ
	 * plane. Size is 2*offset+1 in the two non-fixed axes.
	 */
	public static AxisAlignedBB getFaceFlatBox(Coordinates coords, ForgeDirection direction, int offset)
	{
		if (direction.offsetX != 0)
		{
			return AxisAlignedBB.getBoundingBox(coords.x, coords.y - offset, coords.z - offset,
					coords.x, coords.y + offset, coords.z + offset);
		}
		else if (direction.offsetZ != 0)
		{
			return AxisAlignedBB.getBoundingBox(coords.x - offset, coords.y - offset, coords.z,
					coords.x + offset, coords.y + offset, coords.z);
		}
		return AxisAlignedBB.getBoundingBox(coords.x - offset, coords.y, coords.z - offset,
				coords.x + offset, coords.y, coords.z + offset);
	}

	public static <T extends Entity> T getNewEntityInstance(Class<T> c, World world)
	{
		try
		{
			Constructor<T> constr = c.getConstructor(World.class);
			T ent = constr.newInstance(world);

			if (ent instanceof EntitySkeleton)
			{
				if (world.rand.nextInt(2) == 0)
				{
					((EntitySkeleton) ent).setSkeletonType(1);
					ent.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
				}
				else
				{
					ent.setCurrentItemOrArmor(0, new ItemStack(Items.bow));
				}
			}
			else if (ent instanceof EntityPigZombie)
			{
				ent.setCurrentItemOrArmor(0, new ItemStack(Items.golden_sword));
			}

			return ent;
		}
		catch (Exception e)
		{
			PELogger.logFatal("Could not create new entity instance for: "+c.getCanonicalName());
			e.printStackTrace();
		}

		return null;
	}

	public static EntityLiving getRandomEntity(World world, EntityLiving toRandomize)
	{
		Class<? extends EntityLiving> entClass = toRandomize.getClass();

		if (peacefuls.contains(entClass))
		{
			return getNewEntityInstance(CollectionHelper.getRandomListEntry(peacefuls, entClass), world);
		}
		else if (mobs.contains(entClass))
		{
			return getNewEntityInstance(CollectionHelper.getRandomListEntry(mobs, entClass), world);
		}
		else if (world.rand.nextInt(2) == 0)
		{
			return new EntitySlime(world);
		}
		else
		{
			return new EntitySheep(world);
		}
	}

	public static List<TileEntity> getTileEntitiesWithinAABB(World world, AxisAlignedBB bBox)
	{
		List<TileEntity> list = Lists.newArrayList();

		for (int i = (int) bBox.minX; i <= bBox.maxX; i++)
			for (int j = (int) bBox.minY; j <= bBox.maxY; j++)
				for (int k = (int) bBox.minZ; k <= bBox.maxZ; k++)
				{
					TileEntity tile = world.getBlockTileEntity(i, j, k);
					if (tile != null)
					{
						list.add(tile);
					}
				}

		return list;
	}

	/**
	 * Gravitates an entity, vanilla xp orb style, towards a position
	 * Code adapted from EntityXPOrb and OpenBlocks Vacuum Hopper, mostly the former
	 */
	public static void gravitateEntityTowards(Entity ent, double x, double y, double z)
	{
		double dX = x - ent.posX;
		double dY = y - ent.posY;
		double dZ = z - ent.posZ;
		double dist = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

		double vel = 1.0 - dist / 15.0;
		if (vel > 0.0D)
		{
			vel *= vel;
			ent.motionX += dX / dist * vel * 0.05;
			ent.motionY += dY / dist * vel * 0.1;
			ent.motionZ += dZ / dist * vel * 0.05;
			ent.moveEntity(ent.motionX, ent.motionY, ent.motionZ);
		}
	}

	public static void growNearbyRandomly(boolean harvest, World world, double xCoord, double yCoord, double zCoord, EntityPlayer player)
	{
		int chance = harvest ? 16 : 32;

		for (int x = (int) (xCoord - 5); x <= xCoord + 5; x++)
			for (int y = (int) (yCoord - 3); y <= yCoord + 3; y++)
				for (int z = (int) (zCoord - 5); z <= zCoord + 5; z++)
				{
					Block crop = world.getBlock(x, y, z);

					if (crop == null)
					{
						continue;
					}

					// ---- MITE native plants. MITE's crop/plant blocks do not
					// implement the 1.7.10 growth interfaces the original
					// ProjectE relied on, so they are driven through MITE's own
					// growth system instead.

					// Wheat, carrot, potato, onion: MITE crops store their growth
					// in metadata bits (0-7) and mature crops can be harvested.
					if (crop instanceof BlockCrops)
					{
						BlockCrops crops = (BlockCrops) crop;
						int meta = world.getBlockMetadata(x, y, z);

						if (crops.isDead())
						{
							continue;
						}

						if (harvest && crops.isMature(meta))
						{
							harvestMatureCrop(world, x, y, z);
						}
						else if (world.rand.nextInt(chance) == 0)
						{
							// Random one-stage growth like the original ring;
							// not limited by light or other MITE conditions.
							int newMeta = crops.incrementGrowth(meta);
							if (newMeta != meta)
							{
								world.setBlockMetadataWithNotify(x, y, z, newMeta, 2);
							}
						}
					}
					// Saplings: MITE marks them on the first growth event and
					// turns them into trees on the second one.
					else if (crop instanceof BlockSapling)
					{
						if (world.rand.nextInt(chance) == 0)
						{
							((BlockSapling) crop).markOrGrowMarked(world, x, y, z, world.rand);
						}
					}
					// Reeds and cactus grow taller through their own updateTick.
					else if (crop instanceof BlockReed || crop instanceof BlockCactus)
					{
						if (world.rand.nextInt(chance / 4) == 0)
						{
							for (int i = 0; i < (harvest ? 8 : 4); i++)
							{
								crop.updateTick(world, x, y, z, world.rand);
							}
						}

						if (harvest)
						{
							boolean shouldHarvest = true;
							for (int i = 1; i < 3; i++)
							{
								if (world.getBlock(x, y + i, z) != crop)
								{
									shouldHarvest = false;
									break;
								}
							}

							if (shouldHarvest)
							{
								for (int i = crop instanceof BlockReed ? 1 : 0; i < 3; i++)
								{
									breakBlockWithDrops(world, player, x, y + i, z);
								}
							}
						}
					}
					// Nether wart grows through metadata stages; only break it
					// when it has reached the final stage (metadata 3).
					else if (crop instanceof BlockNetherStalk)
					{
						if (world.rand.nextInt(chance / 4) == 0)
						{
							for (int i = 0; i < (harvest ? 8 : 4); i++)
							{
								crop.updateTick(world, x, y, z, world.rand);
							}
						}
						if (harvest && world.getBlockMetadata(x, y, z) == 3)
						{
							breakBlockWithDrops(world, player, x, y, z);
						}
					}
					// Stems, mushrooms, cocoa, vines, tall grass, dead bushes,
					// flowers, lily pads, melons and pumpkins all grow/spread
					// through MITE's own updateTick.
					else if (crop instanceof BlockStem || crop instanceof BlockMushroom || crop instanceof BlockCocoa
							|| crop instanceof BlockVine || crop instanceof BlockTallGrass || crop instanceof BlockDeadBush
							|| crop instanceof BlockFlower || crop instanceof BlockLilyPad
							|| crop instanceof BlockMelon || crop instanceof BlockPumpkin)
					{
						if (world.rand.nextInt(chance / 4) == 0)
						{
							for (int i = 0; i < (harvest ? 8 : 4); i++)
							{
								crop.updateTick(world, x, y, z, world.rand);
							}
						}

						// Harvest flowers, mushrooms, vines, tall grass, dead
						// bushes and lily pads, but leave stems/fruit alone.
						if (harvest && !(crop instanceof BlockStem)
								&& !(crop instanceof BlockMelon) && !(crop instanceof BlockPumpkin))
						{
							breakBlockWithDrops(world, player, x, y, z);
						}
					}
					// ---- Legacy 1.7.10 fallbacks, kept for any modded plants
					// that still implement the ported interfaces.
					else if (crop instanceof IShearable)
					{
						if (harvest)
						{
							if (player != null && PlayerHelper.hasBreakPermission(((ServerPlayer) player), x, y, z))
							{
								world.setBlockToAir(x, y, z);
							} else if (player == null)
							{
								world.setBlockToAir(x, y, z);
							}
						}
					}
					else if (crop instanceof IGrowable)
					{
						IGrowable growable = (IGrowable) crop;
						if (harvest && !growable.func_149851_a(world, x, y, z, false))
						{
							if (player != null && PlayerHelper.hasBreakPermission(((ServerPlayer) player), x, y, z))
							{
								world.setBlockToAir(x, y, z);
							} else if (player == null)
							{
								world.setBlockToAir(x, y, z);
							}
						}
						else if (world.rand.nextInt(chance) == 0)
						{
							if (ProjectEConfig.harvBandGrass || !crop.getUnlocalizedName().toLowerCase(Locale.ROOT).contains("grass"))
							{
								growable.func_149853_b(world, world.rand, x, y, z);
							}
						}
					}
					else if (crop instanceof IPlantable)
					{
						if (world.rand.nextInt(chance / 4) == 0)
						{
							for (int i = 0; i < (harvest ? 8 : 4); i++)
							{
								crop.updateTick(world, x, y, z, world.rand);
							}
						}

						if (harvest)
						{
							if (crop instanceof BlockFlower)
							{
								if (player != null && PlayerHelper.hasBreakPermission(((ServerPlayer) player), x, y, z))
								{
									world.setBlockToAir(x, y, z);
								} else if (player == null)
								{
									world.setBlockToAir(x, y, z);
								}
							}
							if (crop == Blocks.reeds || crop == Blocks.cactus)
							{
								boolean shouldHarvest = true;

								for (int i = 1; i < 3; i++)
								{
									if (world.getBlock(x, y + i, z) != crop)
									{
										shouldHarvest = false;
										break;
									}
								}

								if (shouldHarvest)
								{
									for (int i = crop == Blocks.reeds ? 1 : 0; i < 3; i++)
									{
										if (player != null && PlayerHelper.hasBreakPermission(((ServerPlayer) player), x, y + i, z))
										{
											world.setBlockToAir(x, y + i, z);
										} else if (player == null)
										{
											world.setBlockToAir(x, y + i, z);
										}
									}
								}
							}
							if (crop == Blocks.nether_wart)
							{
								int meta = ((IPlantable) crop).getPlantMetadata(world, x, y, z);
								if (meta == 3)
								{
									if (player != null && PlayerHelper.hasBreakPermission(((ServerPlayer) player), x, y, z))
									{
										world.setBlockToAir(x, y, z);
									} else if (player == null)
									{
										world.setBlockToAir(x, y, z);
									}
								}
							}
						}
					}
				}
	}

	/**
	 * Cures every blighted crop in the area around the given point (MITE
	 * "sick" crops). Used by the Harvest Goddess ring while carried.
	 */
	public static void cureBlightedCrops(World world, double xCoord, double yCoord, double zCoord)
	{
		for (int x = (int) (xCoord - 5); x <= xCoord + 5; x++)
			for (int y = (int) (yCoord - 3); y <= yCoord + 3; y++)
				for (int z = (int) (zCoord - 5); z <= zCoord + 5; z++)
				{
					Block crop = world.getBlock(x, y, z);
					if (crop instanceof BlockCrops)
					{
						BlockCrops crops = (BlockCrops) crop;
						int meta = world.getBlockMetadata(x, y, z);
						if (!crops.isDead() && crops.isBlighted(meta))
						{
							crops.setBlighted(world, x, y, z, false);
						}
					}
				}
	}

	/**
	 * Harvests every mature crop in the area around the given point: the
	 * harvest goes into the nearest chest (vanilla chest or alchemical chest),
	 * leftover items drop on the ground, and the crop is immediately replanted
	 * at its first growth stage.
	 */
	public static void harvestNearbyMatureCrops(World world, double xCoord, double yCoord, double zCoord)
	{
		for (int x = (int) (xCoord - 5); x <= xCoord + 5; x++)
			for (int y = (int) (yCoord - 3); y <= yCoord + 3; y++)
				for (int z = (int) (zCoord - 5); z <= zCoord + 5; z++)
				{
					harvestMatureCrop(world, x, y, z);
				}
	}

	/**
	 * Harvests a single mature crop block: collects the real MITE drops, puts
	 * them in a nearby chest when possible, then replants the crop.
	 */
	private static void harvestMatureCrop(World world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		if (!(block instanceof BlockCrops))
		{
			return;
		}
		BlockCrops crops = (BlockCrops) block;
		int meta = world.getBlockMetadata(x, y, z);
		if (crops.isDead() || !crops.isMature(meta) || crops.isBlighted(meta))
		{
			return;
		}

		// Mature MITE crops drop their crop item (mature yield) and no seeds;
		// the ring replants automatically, so no seed is required.
		int cropId = net.minecraft.PEPlantCompat.getCropItem(crops);
		int yield = net.minecraft.PEPlantCompat.getCropMatureYield(crops);
		if (cropId > 0 && yield > 0)
		{
			ItemStack drop = new ItemStack(cropId, yield, 0);
			if (!storeInNearbyChest(world, x, y, z, drop))
			{
				spawnEntityItem(world, drop, x, y, z);
			}
		}

		// Replant the same crop at its first growth stage.
		world.setBlock(x, y, z, block.blockID, 0, 3);
	}

	/**
	 * Tries to store the stack into the nearest vanilla or alchemical chest.
	 * Returns true when the whole stack was stored.
	 */
	private static boolean storeInNearbyChest(World world, int x, int y, int z, ItemStack stack)
	{
		for (int i = x - 8; i <= x + 8; i++)
			for (int j = y - 4; j <= y + 4; j++)
				for (int k = z - 8; k <= z + 8; k++)
				{
					TileEntity te = world.getBlockTileEntity(i, j, k);
					if (te instanceof TileEntityChest || te instanceof AlchChestTile)
					{
						if (addStackToInventory((IInventory) te, stack))
						{
							return true;
						}
					}
				}
		return false;
	}

	private static boolean addStackToInventory(IInventory inv, ItemStack stack)
	{
		int limit = inv.getInventoryStackLimit();
		for (int i = 0; i < inv.getSizeInventory() && stack.stackSize > 0; i++)
		{
			ItemStack slot = inv.getStackInSlot(i);
			if (slot != null && slot.itemID == stack.itemID && slot.getItemSubtype() == stack.getItemSubtype()
					&& slot.stackSize < limit)
			{
				int move = Math.min(limit - slot.stackSize, stack.stackSize);
				slot.stackSize += move;
				stack.stackSize -= move;
				inv.onInventoryChanged();
			}
		}
		for (int i = 0; i < inv.getSizeInventory() && stack.stackSize > 0; i++)
		{
			if (inv.getStackInSlot(i) == null && inv.isItemValidForSlot(i, stack))
			{
				ItemStack put = stack.copy();
				put.stackSize = Math.min(limit, put.stackSize);
				stack.stackSize -= put.stackSize;
				inv.setInventorySlotContents(i, put);
			}
		}
		return stack.stackSize <= 0;
	}

	/**
	 * MITE-native block break that produces the block's normal drops.
	 * Only used on the server side (the ring paths are all server gated).
	 */
	private static void breakBlockWithDrops(World world, EntityPlayer player, int x, int y, int z)
	{
		if (world.getBlock(x, y, z) == null)
		{
			return;
		}
		if (player != null && !PlayerHelper.hasBreakPermission(((ServerPlayer) player), x, y, z))
		{
			return;
		}
		world.destroyBlock(new BlockBreakInfo(world, x, y, z), true);
	}

	/**
	 * Recursively mines out a vein of the given Block, starting from the provided coordinates
	 */
	public static void harvestVein(World world, EntityPlayer player, ItemStack stack, Coordinates coords, Block target, List<ItemStack> currentDrops, int numMined)
	{
		if (numMined >= Constants.MAX_VEIN_SIZE)
		{
			return;
		}

		AxisAlignedBB b = AxisAlignedBB.getBoundingBox(coords.x - 1, coords.y - 1, coords.z - 1, coords.x + 1, coords.y + 1, coords.z + 1);

		for (int x = (int) b.minX; x <= b.maxX; x++)
			for (int y = (int) b.minY; y <= b.maxY; y++)
				for (int z = (int) b.minZ; z <= b.maxZ; z++)
				{
					Block block = world.getBlock(x, y, z);

					if (block == target || (target == Blocks.lit_redstone_ore && block == Blocks.redstone_ore))
					{
						numMined++;
						if (PlayerHelper.hasBreakPermission(((ServerPlayer) player), x, y, z))
						{
							currentDrops.addAll(moze_intel.projecte.gameObjs.items.tools.PEToolBase.getAoeDrops(world, stack, player, block, x, y, z));
							world.setBlockToAir(x, y, z);
							harvestVein(world, player, stack, new Coordinates(x, y, z), target, currentDrops, numMined);
						}
					}
				}
	}
	
	public static void igniteNearby(World world, EntityPlayer player)
	{
		for (int x = (int) (player.posX - 8); x <= player.posX + 8; x++)
			for (int y = (int) (player.posY - 5); y <= player.posY + 5; y++)
				for (int z = (int) (player.posZ - 8); z <= player.posZ + 8; z++)
					if (world.rand.nextInt(128) == 0 && world.isAirBlock(x, y, z))
					{
						PlayerHelper.checkedPlaceBlock(((ServerPlayer) player), x, y, z, Blocks.fire, 0);
					}
	}

	public static boolean isArrowInGround(EntityArrow arrow)
	{
		return arrow.isInGround();
	}

	/**
	 * Repels projectiles and mobs in the given AABB away from a given point
	 */
	public static void repelEntitiesInAABBFromPoint(World world, AxisAlignedBB effectBounds, double x, double y, double z, boolean isSWRG)
	{
		List<Entity> list = world.getEntitiesWithinAABB(Entity.class, effectBounds);

		for (Entity ent : list)
		{
			if ((isSWRG && !swrgBlacklist.contains(ent.getClass()))
					|| (!isSWRG && !interdictionBlacklist.contains(ent.getClass()))) {
				if ((ent instanceof EntityLiving) || (ent instanceof IProjectile))
				{
					if (!isSWRG && ProjectEConfig.interdictionMode && !(ent instanceof IMob || ent instanceof IProjectile))
					{
						continue;
					}
					else
					{
						if (ent instanceof EntityArrow && ((EntityArrow) ent).onGround)
						{
							continue;
						}
						Vec3 p = Vec3.createVectorHelper(x, y, z);
						Vec3 t = Vec3.createVectorHelper(ent.posX, ent.posY, ent.posZ);
						double distance = p.distanceTo(t) + 0.1D;

						Vec3 r = Vec3.createVectorHelper(t.xCoord - p.xCoord, t.yCoord - p.yCoord, t.zCoord - p.zCoord);

						// MITE mob AI overwrites velocity every tick, so push the
						// position directly as well - otherwise the repel can be
						// completely erased before the mob's next AI step.
						ent.motionX += r.xCoord / 0.45D / distance;
						ent.motionY += r.yCoord / 0.45D / distance;
						ent.motionZ += r.zCoord / 0.45D / distance;
						if (!isSWRG)
						{
							ent.posX += r.xCoord * 0.02D;
							ent.posY += r.yCoord * 0.02D;
							ent.posZ += r.zCoord * 0.02D;
						}
					}
				}
			}
		}
	}

	public static void spawnEntityItem(World world, ItemStack stack, double x, double y, double z)
	{
		float f = world.rand.nextFloat() * 0.8F + 0.1F;
		float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
		EntityItem entityitem;

		for (float f2 = world.rand.nextFloat() * 0.8F + 0.1F; stack.stackSize > 0; world.spawnEntityInWorld(entityitem))
		{
			int j1 = world.rand.nextInt(21) + 10;

			if (j1 > stack.stackSize)
				j1 = stack.stackSize;

			stack.stackSize -= j1;
			entityitem = new EntityItem(world, (double)((float) x + f), (double)((float) y + f1), (double)((float) z + f2), new ItemStack(stack.getItem(), j1, stack.getItemDamage()));
			float f3 = 0.05F;
			entityitem.motionX = (double)((float) world.rand.nextGaussian() * f3);
			entityitem.motionY = (double)((float) world.rand.nextGaussian() * f3 + 0.2F);
			entityitem.motionZ = (double)((float) world.rand.nextGaussian() * f3);

			if (stack.hasTagCompound())
			{
				entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
			}
		}

	}
}
