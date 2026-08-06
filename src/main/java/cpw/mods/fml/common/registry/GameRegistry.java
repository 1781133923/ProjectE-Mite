package cpw.mods.fml.common.registry;

import cpw.mods.fml.common.IFuelHandler;
import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.Block;
import net.minecraft.CraftingManager;
import net.minecraft.FurnaceRecipes;
import net.minecraft.IRecipe;
import net.minecraft.Item;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;
import net.minecraft.TileEntity;
import net.xiaoyu233.fml.api.item.IItem;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Forge GameRegistry stand-in. Registrations are queued and applied when the FML
 * reload events fire (see moze_intel.projecte.MITEEventBridge).
 */
public class GameRegistry {
    private static final List<ItemRegistration> ITEM_QUEUE = new ArrayList<>();
    private static final List<BlockRegistration> BLOCK_QUEUE = new ArrayList<>();
    private static final List<ShapedRecipeRegistration> SHAPED_QUEUE = new ArrayList<>();
    private static final List<ShapelessRecipeRegistration> SHAPELESS_QUEUE = new ArrayList<>();
    private static final List<IRecipe> CUSTOM_RECIPES = new ArrayList<>();
    private static final List<SmeltingRegistration> SMELTING_QUEUE = new ArrayList<>();
    private static final List<TileEntityRegistration> TILE_ENTITY_QUEUE = new ArrayList<>();
    private static final List<IFuelHandler> FUEL_HANDLERS = new ArrayList<>();
    private static final Map<String, Item> ITEM_ALIASES = new HashMap<>();
    private static final Map<String, Block> BLOCK_ALIASES = new HashMap<>();
    private static final List<Runnable> DRAIN_LISTENERS = new ArrayList<>();

    public static void addDrainListener(Runnable listener) {
        DRAIN_LISTENERS.add(listener);
    }

    /* ------------------------------------------------------------------ */
    /* Item registration                                                  */
    /* ------------------------------------------------------------------ */

    public static void registerItem(Item item, String name) {
        registerItem(item, name, null);
    }

    public static void registerItem(Item item, String name, String modId) {
        // Items take their ID from IdUtil.getNextItemID() in their constructor
        // (see ItemPE) - same pattern as MoreMetals. Nothing to assign here.
        ITEM_QUEUE.add(new ItemRegistration(normalizeName(name), item));
    }

    public static Item registerItem(Item item, String name, String modId, int iconIndex) {
        registerItem(item, name, modId);
        return item;
    }

    public static void registerItem(Item item, String name, int iconIndex) {
        registerItem(item, name, null);
    }

    /* ------------------------------------------------------------------ */
    /* Block registration                                                 */
    /* ------------------------------------------------------------------ */

    public static void registerBlock(Block block, String name) {
        registerBlock(block, ItemBlock.class, name);
    }

    public static void registerBlock(Block block, Class<? extends ItemBlock> itemBlockClass, String name) {
        BLOCK_QUEUE.add(new BlockRegistration(normalizeName(name), block, itemBlockClass));
    }

    public static void registerBlock(Block block, String name, String modId) {
        registerBlock(block, ItemBlock.class, name);
    }

    public static void registerBlock(Block block, Class<? extends ItemBlock> itemBlockClass, String name, String modId) {
        registerBlock(block, itemBlockClass, name);
    }

    public static void registerBlock(Block block, String itemBlockClass, String name, String modId) {
        registerBlock(block, name);
    }

    /* ------------------------------------------------------------------ */
    /* Recipes                                                             */
    /* ------------------------------------------------------------------ */

    public static void addRecipe(ItemStack output, Object... recipe) {
        SHAPED_QUEUE.add(new ShapedRecipeRegistration(output, recipe));
    }

    public static void addRecipe(net.minecraftforge.oredict.ShapedOreRecipe recipe) {
        for (Object[] expanded : net.minecraftforge.oredict.OreRecipeHelper.expand(recipe.output, recipe.recipe)) {
            SHAPED_QUEUE.add(new ShapedRecipeRegistration(recipe.output,
                    java.util.Arrays.copyOfRange(expanded, 1, expanded.length)));
        }
    }

    public static void addRecipe(net.minecraftforge.oredict.ShapelessOreRecipe recipe) {
        for (Object[] expanded : net.minecraftforge.oredict.OreRecipeHelper.expandShapeless(recipe.output, recipe.recipe)) {
            SHAPELESS_QUEUE.add(new ShapelessRecipeRegistration(recipe.output,
                    java.util.Arrays.copyOfRange(expanded, 1, expanded.length)));
        }
    }

    public static void addRecipe(IRecipe recipe) {
        if (recipe instanceof net.minecraftforge.oredict.ShapedOreRecipe) {
            net.minecraftforge.oredict.ShapedOreRecipe oreRecipe = (net.minecraftforge.oredict.ShapedOreRecipe) recipe;
            for (Object[] expanded : net.minecraftforge.oredict.OreRecipeHelper.expand(oreRecipe.output, oreRecipe.recipe)) {
                SHAPED_QUEUE.add(new ShapedRecipeRegistration(expanded[0] instanceof ItemStack ? (ItemStack) expanded[0] : oreRecipe.output,
                        java.util.Arrays.copyOfRange(expanded, 1, expanded.length)));
            }
        } else if (recipe instanceof net.minecraftforge.oredict.ShapelessOreRecipe) {
            net.minecraftforge.oredict.ShapelessOreRecipe oreRecipe = (net.minecraftforge.oredict.ShapelessOreRecipe) recipe;
            for (Object[] expanded : net.minecraftforge.oredict.OreRecipeHelper.expandShapeless(oreRecipe.output, oreRecipe.recipe)) {
                SHAPELESS_QUEUE.add(new ShapelessRecipeRegistration(oreRecipe.output,
                        java.util.Arrays.copyOfRange(expanded, 1, expanded.length)));
            }
        } else {
            CUSTOM_RECIPES.add(recipe);
        }
    }

    public static void addShapedRecipe(ItemStack output, Object... recipe) {
        addRecipe(output, recipe);
    }

    public static void addShapelessRecipe(ItemStack output, Object... recipe) {
        SHAPELESS_QUEUE.add(new ShapelessRecipeRegistration(output, recipe));
    }

    public static void addSmelting(Item input, ItemStack output, float xp) {
        addSmelting(input.itemID, output);
    }

    public static void addSmelting(ItemStack input, ItemStack output, float xp) {
        addSmelting(input.itemID, output);
    }

    public static void addSmelting(int inputId, ItemStack output) {
        SMELTING_QUEUE.add(new SmeltingRegistration(inputId, output));
    }

    /* ------------------------------------------------------------------ */
    /* Tile entities                                                       */
    /* ------------------------------------------------------------------ */

    public static void registerTileEntity(Class<? extends TileEntity> tileEntityClass, String id) {
        TILE_ENTITY_QUEUE.add(new TileEntityRegistration(tileEntityClass, id));
    }

    public static void registerTileEntityWithAlternatives(Class<? extends TileEntity> tileEntityClass, String id, String... alternatives) {
        TILE_ENTITY_QUEUE.add(new TileEntityRegistration(tileEntityClass, id));
    }

    /* ------------------------------------------------------------------ */
    /* Fuels                                                               */
    /* ------------------------------------------------------------------ */

    public static void registerFuelHandler(IFuelHandler handler) {
        FUEL_HANDLERS.add(handler);
    }

    public static List<IFuelHandler> getFuelHandlers() {
        return FUEL_HANDLERS;
    }

    /* ------------------------------------------------------------------ */
    /* Lookups                                                             */
    /* ------------------------------------------------------------------ */

    public static Item findItem(String modId, String name) {
        String key = modId + ":" + name;
        Item alias = ITEM_ALIASES.get(key);
        if (alias != null) {
            return alias;
        }
        for (Item item : Item.itemsList) {
            if (item == null) {
                continue;
            }
            if (item instanceof IItem && ((IItem) item).getNamespace().equalsIgnoreCase(modId)) {
                String itemName = item.getUnlocalizedName();
                if (itemName != null && itemName.replace("item.", "").equalsIgnoreCase(name)) {
                    return item;
                }
            }
        }
        return null;
    }

    public static Block findBlock(String modId, String name) {
        String key = modId + ":" + name;
        Block alias = BLOCK_ALIASES.get(key);
        if (alias != null) {
            return alias;
        }
        for (Block block : Block.blocksList) {
            if (block == null) {
                continue;
            }
            String blockName = block.getUnlocalizedName();
            if (blockName != null && blockName.replace("tile.", "").equalsIgnoreCase(name)) {
                return block;
            }
        }
        return null;
    }

    public static UniqueIdentifier findUniqueIdentifierFor(Item item) {
        String namespace = item instanceof IItem ? ((IItem) item).getNamespace() : "minecraft";
        String name = item.getUnlocalizedName().replace("item.", "");
        return new UniqueIdentifier(namespace, name);
    }

    public static UniqueIdentifier findUniqueIdentifierFor(Block block) {
        String namespace = "minecraft";
        String name = block.getUnlocalizedName().replace("tile.", "");
        Item item = net.minecraft.Item.getItem(block);
        if (item instanceof IItem && ((IItem) item).hasNamespaceSet()) {
            namespace = ((IItem) item).getNamespace();
        }
        return new UniqueIdentifier(namespace, name);
    }

    public static void addAlias(Item item, String legacyName) {
        ITEM_ALIASES.put(legacyName, item);
    }

    public static void addAlias(Block block, String legacyName) {
        BLOCK_ALIASES.put(legacyName, block);
    }

    /* ------------------------------------------------------------------ */
    /* Drain queues (called by MITEEventBridge)                            */
    /* ------------------------------------------------------------------ */

    public static List<ItemRegistration> drainItemQueue() {
        List<ItemRegistration> result = new ArrayList<>(ITEM_QUEUE);
        ITEM_QUEUE.clear();
        return result;
    }

    public static List<BlockRegistration> drainBlockQueue() {
        List<BlockRegistration> result = new ArrayList<>(BLOCK_QUEUE);
        BLOCK_QUEUE.clear();
        return result;
    }

    public static List<ShapedRecipeRegistration> drainShapedQueue() {
        List<ShapedRecipeRegistration> result = new ArrayList<>(SHAPED_QUEUE);
        SHAPED_QUEUE.clear();
        return result;
    }

    public static List<ShapelessRecipeRegistration> drainShapelessQueue() {
        List<ShapelessRecipeRegistration> result = new ArrayList<>(SHAPELESS_QUEUE);
        SHAPELESS_QUEUE.clear();
        return result;
    }

    public static List<IRecipe> drainCustomRecipes() {
        List<IRecipe> result = new ArrayList<>(CUSTOM_RECIPES);
        CUSTOM_RECIPES.clear();
        return result;
    }

    private static final java.lang.reflect.Method CRAFTING_ADD_RECIPE;
    private static final java.lang.reflect.Method CRAFTING_ADD_SHAPELESS;

    static {
        try {
            CRAFTING_ADD_RECIPE = net.minecraft.CraftingManager.class.getDeclaredMethod(
                    "addRecipe", net.minecraft.ItemStack.class, boolean.class, Object[].class);
            CRAFTING_ADD_RECIPE.setAccessible(true);
            CRAFTING_ADD_SHAPELESS = net.minecraft.CraftingManager.class.getDeclaredMethod(
                    "addShapelessRecipe", net.minecraft.ItemStack.class, boolean.class, Object[].class);
            CRAFTING_ADD_SHAPELESS.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Registers any queued recipes directly through MITE's CraftingManager.
     * Safe to call at any time; queues are cleared so nothing is registered
     * twice even if FML's RecipeRegistryEvent also drains them.
     */
    public static void drainAndRegisterRecipes() {
        net.minecraft.CraftingManager manager = net.minecraft.CraftingManager.getInstance();
        try {
            for (ShapedRecipeRegistration registration : drainShapedQueue()) {
                net.minecraft.IRecipe recipe = (net.minecraft.IRecipe) CRAFTING_ADD_RECIPE.invoke(manager, registration.output(), false, (Object) registration.recipe());
                applyCraftingDefaults(recipe);
            }
            for (ShapelessRecipeRegistration registration : drainShapelessQueue()) {
                net.minecraft.IRecipe recipe = (net.minecraft.IRecipe) CRAFTING_ADD_SHAPELESS.invoke(manager, registration.output(), false, (Object) registration.recipe());
                applyCraftingDefaults(recipe);
            }
            for (IRecipe recipe : drainCustomRecipes()) {
                manager.getRecipeList().add(recipe);
                applyCraftingDefaults(recipe);
            }
		} catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
			throw new RuntimeException("Failed to register ProjectE recipes", e);
		}
		// Apply the projectEO.cfg recipe toggles (they may be changed at
		// runtime via /manylib reload projectEO).
		moze_intel.projecte.config.ProjectEOConfig.markRecipesRegistered();
	}

    /**
     * MITE crafting tier + speed. The listed machines/tools require an iron (or
     * better) workbench; every other ProjectE recipe requires a mithril (or
     * better) workbench. The recipe difficulty controls how long the craft
     * takes: MITE computes period = (difficulty-100)^0.8 + 100 ticks for
     * difficulty > 100, then divides by crafting level + bench modifiers.
     * Iron tier 700 -> base ~13s (actual ~6-15s), mithril tier 4400 -> base
     * ~45s (actual ~20-60s) on the matching bench. Quality above average
     * doubles the difficulty per level, so higher-quality crafts take longer.
     */
    private static void applyCraftingDefaults(net.minecraft.IRecipe recipe) {
        if (recipe == null) {
            return;
        }
        net.minecraft.ItemStack output = recipe.getRecipeOutput();
        if (output == null || output.getItem() == null) {
            return;
        }
        net.minecraft.Item item = output.getItem();
        boolean ironTier = item == ObjHandler.alchBag
                || item == net.minecraft.Item.getItem(ObjHandler.alchChest)
                || item == net.minecraft.Item.getItem(ObjHandler.condenser)
                || item == net.minecraft.Item.getItem(ObjHandler.confuseTorch)
                || item == ObjHandler.philosStone
                || item == ObjHandler.dCatalyst
                || item == ObjHandler.cataliticLens
                || item == ObjHandler.covalence
                || item == net.minecraft.Item.getItem(ObjHandler.transmuteStone)
                || item == ObjHandler.repairTalisman
                || item == ObjHandler.mercEye
                || item == ObjHandler.ironBand
                // Charcoal -> coal exchange (outputs exactly one coal) is an
                // iron-tier craft like the Philosopher's Stone itself. The
                // other coal-output exchange (alchemical coal -> 4 coal)
                // stays mithril-tier.
                || (item == net.minecraft.init.Items.coal && output.stackSize == 1);
        recipe.setMaterialToCheckToolBenchHardnessAgainst(
                ironTier ? net.minecraft.Material.iron : net.minecraft.Material.mithril);
        recipe.setDifficulty(ironTier ? 700.0F : 4400.0F);
    }

    public static List<SmeltingRegistration> drainSmeltingQueue() {
        List<SmeltingRegistration> result = new ArrayList<>(SMELTING_QUEUE);
        SMELTING_QUEUE.clear();
        return result;
    }

    public static List<TileEntityRegistration> drainTileEntityQueue() {
        List<TileEntityRegistration> result = new ArrayList<>(TILE_ENTITY_QUEUE);
        TILE_ENTITY_QUEUE.clear();
        return result;
    }

    public static void applySmelting() {
        for (SmeltingRegistration registration : drainSmeltingQueue()) {
            FurnaceRecipes.smelting().addSmelting(registration.inputId, registration.output);
        }
        drainCustomRecipes().forEach(recipe -> CraftingManager.getInstance().getRecipeList().add(recipe));
        for (Runnable listener : DRAIN_LISTENERS) {
            listener.run();
        }
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String result = name;
        if (result.startsWith("item.")) {
            result = result.substring(5);
        }
        if (result.startsWith("tile.")) {
            result = result.substring(5);
        }
        return result;
    }

    public enum Type {
        ITEM,
        BLOCK
    }

    public static class UniqueIdentifier {
        public final String modId;
        public final String name;

        public UniqueIdentifier(String modId, String name) {
            this.modId = modId;
            this.name = name;
        }

        @Override
        public String toString() {
            return this.modId + ":" + this.name;
        }
    }

    public record ItemRegistration(String name, Item item) {
    }

    public record BlockRegistration(String name, Block block, Class<? extends ItemBlock> itemBlockClass) {
    }

    public record ShapedRecipeRegistration(ItemStack output, Object[] recipe) {
    }

    public record ShapelessRecipeRegistration(ItemStack output, Object[] recipe) {
    }

    public record SmeltingRegistration(int inputId, ItemStack output) {
    }

    public record TileEntityRegistration(Class<? extends TileEntity> clazz, String name) {
    }
}
