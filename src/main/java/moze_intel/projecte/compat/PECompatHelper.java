package moze_intel.projecte.compat;

import net.minecraft.Block;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.xiaoyu233.fml.api.item.IItem;

import java.util.HashMap;
import java.util.Map;

public final class PECompatHelper {
    // 1.7.10-era registry names -> the names MITE actually uses. Applied after
    // prefix normalisation when an exact match was not found.
    private static final Map<String, String> NAME_ALIASES = new HashMap<>();

    static {
        NAME_ALIASES.put("blaze_powder", "blazePowder");
        NAME_ALIASES.put("ender_pearl", "enderPearl");
        NAME_ALIASES.put("soul_sand", "slowSand");
        NAME_ALIASES.put("gold_ingot", "ingotGold");
        NAME_ALIASES.put("iron_ingot", "ingotIron");
        NAME_ALIASES.put("gold_nugget", "goldNugget");
        NAME_ALIASES.put("iron_nugget", "ironNugget");
        NAME_ALIASES.put("dye_powder", "dyePowder");
        NAME_ALIASES.put("nether_wart", "netherStalkSeeds");
        NAME_ALIASES.put("nether_brick", "netherbrick");
        NAME_ALIASES.put("quartz", "netherquartz");
        NAME_ALIASES.put("stone_slab", "stoneSlab");
        NAME_ALIASES.put("stone_slab_double", "stoneSlabDouble");
        // NOTE: no "reeds" alias here on purpose - MITE's sugar cane item is
        // registered as "reeds" (unlocalized "item.reeds"). Mapping it to
        // "reed" used to make the lookup fail, so the sugar cane EMC of 32
        // (LazyMapper) never reached the graph and paper could never be
        // inferred from its recipe.
        NAME_ALIASES.put("waterlily", "lilypad");
    }

    private PECompatHelper() {
    }

    public static Block getBlockFromName(String name) {
        String id = name;
        int colon = name.indexOf(':');
        if (colon >= 0) {
            id = name.substring(colon + 1);
        }
        // Strip the legacy item./tile. prefixes from the requested name too,
        // so "minecraft:tile.oreCoal" matches a block named "tile.oreCoal".
        id = normalizeName(id);
        for (Block block : Block.blocksList) {
            if (block == null) {
                continue;
            }
            String unlocalized = block.getUnlocalizedName();
            if (unlocalized != null && normalizeName(unlocalized).equalsIgnoreCase(id)) {
                return block;
            }
        }
        return null;
    }

    public static Item getItemFromBlock(Block block) {
        return Item.getItem(block);
    }

    public static Block getBlockFromItem(Item item) {
        return Block.blocksList[item.itemID];
    }

    public static Item getItem(String registryName) {
        if (registryName == null) {
            return null;
        }
        String id = registryName;
        int colon = registryName.indexOf(':');
        if (colon >= 0) {
            id = registryName.substring(colon + 1);
        }
        id = normalizeName(id);
        String aliasedId = aliasOrSelf(id);
        for (Item item : Item.itemsList) {
            if (item == null) {
                continue;
            }
            String name = item.getUnlocalizedName();
            if (name != null && normalizeName(name).equalsIgnoreCase(aliasedId)) {
                return item;
            }
        }
        // Fall back to block items (some blocks only exist as ItemBlocks in itemsList).
        for (Block block : Block.blocksList) {
            if (block == null) {
                continue;
            }
            String name = block.getUnlocalizedName();
            if (name != null && normalizeName(name).equalsIgnoreCase(aliasedId)) {
                Item blockItem = Item.getItem(block);
                if (blockItem != null) {
                    return blockItem;
                }
            }
        }
        return null;
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        return name.replace("item.", "").replace("tile.", "");
    }

    private static String aliasOrSelf(String name) {
        if (name == null) {
            return null;
        }
        String aliased = NAME_ALIASES.get(name.toLowerCase());
        return aliased != null ? aliased : name;
    }

    public static String getItemName(Item item) {
        if (item == null) {
            return "null";
        }
        String namespace = item instanceof IItem ? ((IItem) item).getNamespace() : "minecraft";
        String name = item.getUnlocalizedName() == null ? String.valueOf(item.itemID) : item.getUnlocalizedName().replace("item.", "");
        return namespace + ":" + name;
    }

    public static boolean isFuel(ItemStack stack) {
        return stack != null && stack.getItem() != null && stack.getItem().getBurnTime(stack) > 0;
    }

    public static int getBurnTime(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return 0;
        }
        return stack.getItem().getBurnTime(stack);
    }

    /**
     * MITE does not expose vanilla rain toggling; this is a best-effort hook
     * (currently a no-op, see PORT.md).
     */
    public static void setRaining(net.minecraft.World world, boolean raining) {
        if (world != null) {
            // MITE's weather event system re-drives the strength each tick, so
            // this is a best-effort immediate change (ramps in/out over ticks).
            world.setRainStrength(raining ? 1.0F : 0.0F);
        }
    }

    public static void setRainTime(net.minecraft.World world, int time) {
        // MITE has no rain-duration counters (weather events are time based).
    }

    public static void setThunderTime(net.minecraft.World world, int time) {
        // MITE has no thunder-duration counters.
    }

    public static void setThundering(net.minecraft.World world, boolean thundering) {
        // MITE's thunder is driven by weather events; rain strength is the
        // only directly settable weather value.
    }

    public static void setWorldTime(net.minecraft.World world, long time) {
        if (world == null || world.getWorldInfo() == null) {
            return;
        }
        // MITE keeps a separate total world time per dimension; the time of day
        // is totalTime % 24000. TimeWatch passes the new total world time here.
        world.getWorldInfo().setTotalWorldTime(time, world);
    }

    public static boolean isItemEqual(ItemStack first, ItemStack second) {
        if (first == null || second == null) {
            return false;
        }
        if (first.itemID != second.itemID) {
            return false;
        }
        int dmg1 = first.getItemSubtype();
        int dmg2 = second.getItemSubtype();
        return dmg1 == Short.MAX_VALUE || dmg2 == Short.MAX_VALUE || dmg1 == dmg2;
    }

    public static void fillCauldron(net.minecraft.World world, int x, int y, int z, int level) {
        if (world != null) {
            world.setBlockMetadataWithNotify(x, y, z, level, 2);
        }
    }

    public static void feedPlayer(net.minecraft.EntityPlayer player) {
        if (player != null && !player.worldObj.isRemote) {
            // MITE's food model: eating adds both satiation (the buffer that is
            // drained first when hungry) and nutrition (the food bar itself).
            // Only touching satiation never moves the visible bar, which is why
            // the old ported code appeared to do nothing.
            net.minecraft.FoodStats stats = player.getFoodStats();
            stats.addSatiation(2);
            stats.addNutrition(1);
        }
    }

    public static net.minecraft.MovingObjectPosition getMovingObjectPositionFromPlayer(net.minecraft.World world, net.minecraft.EntityPlayer player) {
        net.minecraft.RaycastCollision rc = player.getSelectedObject(1.0F, false);
        if (rc == null)
        {
            return null;
        }
        if (rc.isBlock())
        {
            return new net.minecraft.MovingObjectPosition(world, rc.block_hit_x, rc.block_hit_y, rc.block_hit_z,
                    rc.face_hit.ordinal(), rc.position_hit);
        }
        if (rc.isEntity())
        {
            net.minecraft.Entity entity = rc.getEntityHit();
            if (entity != null)
            {
                return new net.minecraft.MovingObjectPosition(entity, player.getDistanceToEntity(entity));
            }
        }
        return null;
    }

    public static net.minecraft.EnumParticle getParticle(String name) {
        if (name == null) {
            return net.minecraft.EnumParticle.smoke;
        }
        try {
            if (name.equals("portal")) {
                return net.minecraft.EnumParticle.portal_nether;
            }
            return net.minecraft.EnumParticle.valueOf(name);
        } catch (IllegalArgumentException e) {
            return net.minecraft.EnumParticle.smoke;
        }
    }

    public static net.minecraft.MovingObjectPosition rayTrace(net.minecraft.World world, net.minecraft.Vec3 from, net.minecraft.Vec3 to) {
        net.minecraft.Raycast raycast = new net.minecraft.Raycast(world, from, to);
        raycast.performVsBlocks();
        if (raycast.hasBlockCollision())
        {
            net.minecraft.RaycastCollision rc = raycast.getBlockCollision();
            return new net.minecraft.MovingObjectPosition(world, rc.block_hit_x, rc.block_hit_y, rc.block_hit_z,
                    rc.face_hit.ordinal(), rc.position_hit);
        }
        return null;
    }
}
