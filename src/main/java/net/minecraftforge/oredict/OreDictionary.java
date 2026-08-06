package net.minecraftforge.oredict;

import net.minecraft.Block;
import net.minecraft.Item;
import net.minecraft.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight OreDictionary stand-in. Seeded heuristically from MITE's item/block
 * registries so EMC mapping and recipes have a working ore concept.
 */
public class OreDictionary {
    public static final int WILDCARD_VALUE = Short.MAX_VALUE;

    private static final Map<String, List<ItemStack>> ORES = new HashMap<>();
    private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();
    private static boolean initialized;

    public static void registerOre(String name, ItemStack ore) {
        ORES.computeIfAbsent(name, k -> new ArrayList<>()).add(ore);
    }

    public static List<ItemStack> getOres(String name) {
        ensureInitialized();
        List<ItemStack> result = ORES.get(name);
        return result == null ? new ArrayList<>() : new ArrayList<>(result);
    }

    public static String[] getOreNames() {
        ensureInitialized();
        return ORES.keySet().toArray(new String[0]);
    }

    public static int getOreID(String name) {
        ensureInitialized();
        for (Map.Entry<Integer, String> entry : ID_TO_NAME.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        int id = ID_TO_NAME.size() + 1;
        ID_TO_NAME.put(id, name);
        return id;
    }

    public static int[] getOreIDs(ItemStack stack) {
        ensureInitialized();
        List<Integer> ids = new ArrayList<>();
        for (Map.Entry<String, List<ItemStack>> entry : ORES.entrySet()) {
            for (ItemStack ore : entry.getValue()) {
                if (itemMatches(ore, stack, false)) {
                    ids.add(getOreID(entry.getKey()));
                    break;
                }
            }
        }
        int[] result = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            result[i] = ids.get(i);
        }
        return result;
    }

    public static String getOreName(int id) {
        ensureInitialized();
        return ID_TO_NAME.getOrDefault(id, "Unknown");
    }

    public static boolean itemMatches(ItemStack target, ItemStack input, boolean matchDamage) {
        if (target == null || input == null) {
            return false;
        }
        if (target.itemID != input.itemID) {
            return false;
        }
        if (matchDamage) {
            return target.getItemSubtype() == input.getItemSubtype();
        }
        int targetDamage = target.getItemSubtype();
        return targetDamage == WILDCARD_VALUE || targetDamage == input.getItemSubtype();
    }

    private static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }
        initialized = true;
        // Blocks first
        for (Block block : Block.blocksList) {
            if (block == null) {
                continue;
            }
            String name = block.getUnlocalizedName();
            if (name == null) {
                continue;
            }
            name = name.replace("tile.", "");
            registerByPattern(name, new ItemStack(block));
        }
        // Items
        for (Item item : Item.itemsList) {
            if (item == null) {
                continue;
            }
            String name = item.getUnlocalizedName();
            if (name == null) {
                continue;
            }
            name = name.replace("item.", "");
            registerByPattern(name, new ItemStack(item));
        }
        // Explicit common aliases
        registerOre("treeSapling", new ItemStack(Block.sapling, 1, WILDCARD_VALUE));
        registerOre("treeLeaves", new ItemStack(Block.leaves, 1, WILDCARD_VALUE));
        registerOre("logWood", new ItemStack(Block.wood, 1, WILDCARD_VALUE));
        registerOre("plankWood", new ItemStack(Block.planks, 1, WILDCARD_VALUE));
        registerOre("cobblestone", new ItemStack(Block.cobblestone));
        registerOre("stone", new ItemStack(Block.stone));
        registerOre("dirt", new ItemStack(Block.dirt));
        registerOre("sand", new ItemStack(Block.sand));
        registerOre("gravel", new ItemStack(Block.gravel));
        registerOre("blockGlass", new ItemStack(Block.glass));
        registerOre("obsidian", new ItemStack(Block.obsidian));
        registerOre("dustRedstone", new ItemStack(Item.redstone));
        registerOre("dustGlowstone", new ItemStack(Item.glowstone));
        registerOre("gemDiamond", new ItemStack(Item.diamond));
        registerOre("gemEmerald", new ItemStack(Item.emerald));
        registerOre("gemQuartz", new ItemStack(Item.netherQuartz));
        registerOre("itemCoal", new ItemStack(Item.coal, 1, WILDCARD_VALUE));
        registerOre("itemFlint", new ItemStack(Item.flint));
        registerOre("itemClay", new ItemStack(Item.clay));
        registerOre("itemBrick", new ItemStack(Item.brick));
    }

    private static void registerByPattern(String name, ItemStack stack) {
        String ore = null;
        if (name.startsWith("ore")) {
            ore = "ore" + capitalize(name.substring(3));
        } else if (name.startsWith("ingot")) {
            ore = "ingot" + capitalize(name.substring(5));
        } else if (name.startsWith("dust")) {
            ore = "dust" + capitalize(name.substring(4));
        } else if (name.startsWith("gem") && name.length() > 3 && Character.isUpperCase(name.charAt(3))) {
            ore = "gem" + capitalize(name.substring(3));
        } else if (name.startsWith("nugget")) {
            ore = "nugget" + capitalize(name.substring(6));
        } else if (name.startsWith("block") && name.length() > 5 && Character.isUpperCase(name.charAt(5))) {
            ore = "block" + capitalize(name.substring(5));
        } else if (name.startsWith("log") && name.length() > 3 && Character.isUpperCase(name.charAt(3))) {
            ore = "log" + capitalize(name.substring(3));
        }
        if (ore != null) {
            registerOre(ore, stack);
        }
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
