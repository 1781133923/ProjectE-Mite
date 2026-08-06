package moze_intel.projecte;

import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.ItemBlock;
import net.minecraft.ResourceLocation;
import net.xiaoyu233.fml.reload.event.BlockRegistryEvent;
import net.xiaoyu233.fml.reload.event.EntityRegisterEvent;
import net.xiaoyu233.fml.reload.event.ItemRegistryEvent;
import net.xiaoyu233.fml.reload.event.RecipeRegistryEvent;
import net.xiaoyu233.fml.reload.event.SmeltRecipeRegistryEvent;
import net.xiaoyu233.fml.reload.event.SoundsRegisterEvent;
import net.xiaoyu233.fml.reload.event.TileEntityRegisterEvent;

import java.lang.reflect.Constructor;

/**
 * Applies queued Forge-style registrations when FML's reload events fire.
 */
public class MITEEventBridge {
    @Subscribe
    public void onItemRegister(ItemRegistryEvent event) {
        registerQueuedItems(event);
    }

    @Subscribe
    public void onBlockRegister(BlockRegistryEvent event) {
        registerQueuedBlocks(event);
    }

    /**
     * Registers queued items through an ItemRegistryEvent. Safe to call
     * directly (the event is only a data holder) so registration does not
     * depend on when FML happens to construct CraftingManager.
     */
    public static void registerQueuedItems(ItemRegistryEvent event) {
        for (GameRegistry.ItemRegistration registration : GameRegistry.drainItemQueue()) {
            event.register("ProjectE", registration.item(), registration.item().getCreativeTab());
        }
        for (GameRegistry.BlockRegistration registration : GameRegistry.drainBlockQueue()) {
            ItemBlock itemBlock;
            try {
                if (registration.itemBlockClass() == ItemBlock.class) {
                    itemBlock = new ItemBlock(registration.block());
                } else {
                    Constructor<? extends ItemBlock> constructor = registration.itemBlockClass().getConstructor(net.minecraft.Block.class);
                    itemBlock = constructor.newInstance(registration.block());
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to create ItemBlock for " + registration.block(), e);
            }
            itemBlock.setUnlocalizedName(registration.name());
            // This loop drains the block queue before registerQueuedBlocks ever
            // runs, so the block's own namespace would stay at the default
            // "minecraft" (EMI shows it for every ProjectE block item). Set it
            // here together with the ItemBlock's namespace.
            ((net.xiaoyu233.fml.api.block.IBlock) registration.block()).setNamespace("ProjectE");
            event.register("ProjectE", itemBlock, itemBlock.getCreativeTab());
        }
    }

    public static void registerQueuedBlocks(BlockRegistryEvent event) {
        for (GameRegistry.BlockRegistration registration : GameRegistry.drainBlockQueue()) {
            event.registerBlock("ProjectE", registration.block());
        }
    }

    @Subscribe
    public void onRecipeRegister(RecipeRegistryEvent event) {
        java.util.List<GameRegistry.ShapedRecipeRegistration> shaped = GameRegistry.drainShapedQueue();
        java.util.List<GameRegistry.ShapelessRecipeRegistration> shapeless = GameRegistry.drainShapelessQueue();
        for (GameRegistry.ShapedRecipeRegistration registration : shaped) {
            event.registerShapedRecipe(registration.output(), false, registration.recipe());
        }
        for (GameRegistry.ShapelessRecipeRegistration registration : shapeless) {
            event.registerShapelessRecipe(registration.output(), false, registration.recipe());
        }
        java.util.List<net.minecraft.IRecipe> custom = GameRegistry.drainCustomRecipes();
        custom.forEach(recipe -> net.minecraft.CraftingManager.getInstance().getRecipeList().add(recipe));
    }

    @Subscribe
    public void onSmeltRegister(SmeltRecipeRegistryEvent event) {
        GameRegistry.applySmelting();
    }

    /**
     * MITE does not read sounds.json; every sound must be registered with the
     * SoundManager through SoundsRegisterEvent, which fires during the
     * SoundManager constructor (after mod init, so this listener is ready).
     */
    @Subscribe
    public void onSoundRegister(SoundsRegisterEvent event) {
        String[] sounds = {
                "item/pecharge", "item/peuncharge", "item/pedestruct", "item/pepower",
                "item/petransmute", "item/peheal", "item/pewatermagic", "item/pewindmagic",
                "item/chargetick", "item/gust", "item/kinesis", "item/launch", "item/nova",
                "item/philball", "item/tock"
        };
        for (String sound : sounds) {
            event.registerSound(new ResourceLocation("projecte", sound));
        }
    }

    @Subscribe
    public void onTileEntityRegister(TileEntityRegisterEvent event) {
        registerQueuedTileEntities(event);
    }

    /**
     * Registers queued tile entities directly. The FML TileEntityRegisterEvent
     * fires from TileEntity's static initialiser, which can happen before
     * ProjectE's own init; calling the mapping registration directly makes the
     * order irrelevant.
     */
    public static void registerQueuedTileEntities(TileEntityRegisterEvent event) {
        for (GameRegistry.TileEntityRegistration registration : GameRegistry.drainTileEntityQueue()) {
            event.register(registration.clazz(), registration.name());
        }
    }

    @Subscribe
    public void onEntityRegister(EntityRegisterEvent event) {
        for (EntityRegistry.EntityRegistration registration : EntityRegistry.drainQueue()) {
            if (registration.eggColor1() >= 0) {
                event.register(registration.entityClass(), registration.entityName(), PECore.MODID,
                        registration.id(), registration.eggColor1(), registration.eggColor2());
            } else {
                event.register(registration.entityClass(), registration.entityName(), PECore.MODID, registration.id());
            }
        }
    }
}
