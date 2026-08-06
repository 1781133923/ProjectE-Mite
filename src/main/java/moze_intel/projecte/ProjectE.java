package moze_intel.projecte;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import moddedmite.rustedironcore.api.event.Handlers;
import moddedmite.rustedironcore.api.event.listener.ITickListener;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.utils.GuiHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.xiaoyu233.fml.FishModLoader;
import net.xiaoyu233.fml.ModResourceManager;
import net.xiaoyu233.fml.reload.event.MITEEvents;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

/**
 * FML entry point for the ProjectE-MITE port. Drives the Forge-style lifecycle
 * defined in PECore with shim event objects.
 */
public class ProjectE implements ModInitializer {
    public static final String MOD_ID = "projecte";

    private MinecraftServer currentServer;
    private boolean serverStartingFired;
    private static boolean recipeRegistrationDone;

    @Override
    public void onInitialize() {
        ModResourceManager.addResourcePackDomain(MOD_ID);
        MITEEvents.MITE_EVENT_BUS.register(new MITEEventBridge());
        registerCarryOnPluginIfPresent();

        PECore.instance = new PECore();
		PECore.proxy = FishModLoader.isServer()
				? new moze_intel.projecte.proxies.ServerProxy()
				: new moze_intel.projecte.proxies.ClientProxy();
		if (!FishModLoader.isServer())
		{
			// Entity renderers must be queued before Minecraft.startGame()
			// constructs RenderManager (which fires the renderer-register
			// event); see ClientProxy.registerEntityRenderersEarly.
			((moze_intel.projecte.proxies.ClientProxy) PECore.proxy).registerEntityRenderersEarly();
		}
		File configDir = new File("config");
        PECore.instance.preInit(new FMLPreInitializationEvent(configDir));
        if (!FishModLoader.isServer())
        {
            // Run after every mod's main entrypoint has finished (Minecraft
            // startGame completes after all onInitialize calls): only then are
            // all ItemRegistryEvent listeners registered, so constructing
            // CraftingManager now fires the once-only registry events for
            // every mod instead of only the ones that loaded before ProjectE.
            deferClientInit(ProjectE::finalizeRecipeRegistration);
        }
        if (FishModLoader.isServer())
        {
            PECore.instance.load(new FMLInitializationEvent());
        }
        else
        {
            // Standard FishModLoader practice (same pattern as ManyLib's
            // InitializationHandler): client-only setup (keybinds, renderers)
            // must NOT run in the "main" entrypoint, which executes before the
            // Minecraft instance exists. Defer it until Minecraft.startGame()
            // has finished, so Minecraft/RenderManager are ready and render
            // mixins that read Minecraft.getMinecraft() (e.g. BetterGameSetting's
            // RenderMixin) are safe.
            deferClientInit(() -> PECore.instance.load(new FMLInitializationEvent()));
        }
        PECore.instance.postInit(new FMLPostInitializationEvent());
        System.out.println("[ProjectE] Creative tabs registered: " +
                huix.glacier.api.extension.creativetab.GlacierCreativeTabs.newCreativeTabArray.size());

        // FishModLoader never posts the FML PlayerLoggedOutEvent either, so
        // ConnectionHandler.playerDisconnect (which drops the in-memory
        // transmutation/bag props) never ran. Bridge RIC's logout event onto
        // the Forge-shim bus so the cleanup happens when leaving a world.
        Handlers.PlayerEvent.register(new moddedmite.rustedironcore.api.event.listener.IPlayerEventListener() {
            @Override
            public void onPlayerLoggedOut(moddedmite.rustedironcore.api.event.events.PlayerLoggedOutEvent event) {
                if (event.player() != null) {
                    net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                            new cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent(event.player()));
                }
            }
        });

        // Server ticks: drive FML TickEvents + ProjectE server lifecycle.
        Handlers.Tick.register(new ITickListener() {
            @Override
            public void onServerTick(MinecraftServer server) {
                if (ProjectE.this.currentServer != server) {
                    ProjectE.this.currentServer = server;
                    ProjectE.this.serverStartingFired = false;
                }
                if (!ProjectE.this.serverStartingFired) {
                    ProjectE.this.serverStartingFired = true;
                    ProjectE.this.onServerStarting(server);
                }
                FMLCommonHandler.instance().bus().post(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
            }

            @Override
            public void onEntityPlayerTick(net.minecraft.EntityPlayer player) {
                FMLCommonHandler.instance().bus().post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
                // Red matter / gem armour: +100% knockback resistance while
                // worn. Applied to the player attribute only (server side), so
                // monsters that pick up the armour keep just its armour value.
                updateKnockbackResistance(player);
                updateLegsSpeed(player);
                // MITE does not fire Forge's onArmorTick; drive the gem armour
                // per-piece effects from the player tick (client + server).
                for (net.minecraft.ItemStack stack : player.inventory.armorInventory) {
                    if (stack != null && stack.getItem() instanceof moze_intel.projecte.gameObjs.items.armor.GemArmorBase) {
                        ((moze_intel.projecte.gameObjs.items.armor.GemArmorBase) stack.getItem())
                                .onArmorTick(player.worldObj, player, stack);
                    }
                }
                // MITE never calls ItemStack.onUpdate, so every ProjectE item
                // that relies on it (rings, amulets, Repair Talisman, Evertide,
                // Watch of Flowing Time, ...) would never tick. Reproduce the
                // vanilla call for every main-inventory slot on both sides; the
                  // items gate themselves by slot (e.g. rings only work in the
                  // hotbar) just like they did in vanilla.
                  // Multiple copies of the same carried effect item must not
                  // stack (e.g. several Soul Stones healing at once); only the
                  // first copy of each kind ticks per player tick.
                  java.util.Set<net.minecraft.Item> uniqueCarriedEffects = new java.util.HashSet<>();
                  for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
                      net.minecraft.ItemStack stack = player.inventory.getStackInSlot(slot);
                      if (stack != null) {
                          net.minecraft.Item item = stack.getItem();
                          if (isUniqueCarriedEffect(item) && !uniqueCarriedEffects.add(item)) {
                              continue;
                          }
                          stack.getItem().onUpdate(stack, player.worldObj, player, slot,
                                  slot == player.inventory.currentItem);
                      }
                  }
              }
          });
      }

      /**
       * Defers client-only setup until Minecraft.startGame() has finished.
       * ManyLib's InitializationHandler is used when it is installed (via
       * reflection, so there is no compile/load dependency); otherwise it falls
       * back to RustedIronCore's client-started event, which fires with the
       * Minecraft instance ready. Removing ManyLib therefore never breaks or
       * crashes the mod.
       */
      private static void deferClientInit(Runnable runnable)
      {
          try
          {
              Class<?> initClass = Class.forName("fi.dy.masa.malilib.event.InitializationHandler");
              Class<?> handlerIface = Class.forName("fi.dy.masa.malilib.interfaces.IInitializationHandler");
              Object dispatcher = initClass.getMethod("getInstance").invoke(null);
              Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                      handlerIface.getClassLoader(),
                      new Class<?>[]{handlerIface},
                      (p, method, args) ->
                      {
                          if ("registerModHandlers".equals(method.getName()))
                          {
                              runnable.run();
                              return null;
                          }
                          if (method.getDeclaringClass() == Object.class)
                          {
                              switch (method.getName())
                              {
                                  case "toString":
                                      return p.getClass().getName();
                                  case "hashCode":
                                      return System.identityHashCode(p);
                                  case "equals":
                                      return p == args[0];
                              }
                          }
                          return null;
                      });
              initClass.getMethod("registerInitializationHandler", handlerIface).invoke(dispatcher, proxy);
              return;
          }
          catch (Throwable malilibMissing)
          {
              // Fall back to RIC below.
          }
          moddedmite.rustedironcore.api.event.Handlers.Initialization.register(
                  new moddedmite.rustedironcore.api.event.listener.IInitializationListener()
                  {
                      @Override
                      public void onClientStarted(net.minecraft.Minecraft mc)
                      {
                          runnable.run();
                      }
                  });
      }

      /**
       * MITE's knockback check rolls rand.nextDouble() against
       * SharedMonsterAttributes.knockbackResistance (a 0..1 ranged
       * attribute), so a value of 1.0 fully blocks knockback. A single +1.0
       * modifier is applied while the player wears any red matter or gem
       * armour piece, and removed when the last piece is taken off. The
       * modifier UUID is fixed, so applying/removing is idempotent and never
       * throws ModifiableAttributeInstance's "already applied" error.
       */
      private static final java.util.UUID KNOCKBACK_RESISTANCE_UUID =
              java.util.UUID.fromString("6F3A2D1C-9E45-4B7A-9D0E-3C1B8A4F7E21");
      private static final net.minecraft.AttributeModifier KNOCKBACK_RESISTANCE_MODIFIER =
              new net.minecraft.AttributeModifier(KNOCKBACK_RESISTANCE_UUID, "ProjectE knockback resistance", 1.0, 0);

      /**
       * Dark matter / red matter / gem leggings: +10% / +20% / +30% movement
       * speed while worn. EntityPlayer.getAIMoveSpeed() returns the
       * movementSpeed attribute value, so a multiply-base modifier (op 1)
       * gives exactly the requested percentage. Applied on both sides so the
       * client's local movement prediction matches the server.
       */
      private static final java.util.UUID LEGS_SPEED_UUID =
              java.util.UUID.fromString("9B4A2C1E-7D35-4F6A-9B0C-1E2F3A4B5C6D");
      private static final java.util.Map<String, Float> LEGS_BASE_FLY_SPEED = new java.util.HashMap<>();

      private static void updateLegsSpeed(net.minecraft.EntityPlayer player)
      {
          double bonus = 0.0D;
          // MITE armor slot order (confirmed by runtime debug output):
          // 0=feet, 1=legs, 2=chest, 3=helmet.
          net.minecraft.ItemStack legs = player.inventory.armorInventory[1];
          if (legs != null)
          {
              net.minecraft.Item item = legs.getItem();
              if (item instanceof moze_intel.projecte.gameObjs.items.armor.DMArmor
                      && ((moze_intel.projecte.gameObjs.items.armor.DMArmor) item).getArmorPiece()
                              == moze_intel.projecte.utils.EnumArmorType.LEGS)
              {
                  bonus = 0.1D;
              }
              else if (item instanceof moze_intel.projecte.gameObjs.items.armor.RMArmor
                      && ((moze_intel.projecte.gameObjs.items.armor.RMArmor) item).getArmorPiece()
                              == moze_intel.projecte.utils.EnumArmorType.LEGS)
              {
                  bonus = 0.2D;
              }
              else if (item instanceof moze_intel.projecte.gameObjs.items.armor.GemArmorBase
                      && ((moze_intel.projecte.gameObjs.items.armor.GemArmorBase) item).getArmorPiece()
                              == moze_intel.projecte.utils.EnumArmorType.LEGS)
              {
                  bonus = 0.3D;
              }
          }
          net.minecraft.AttributeInstance attribute =
                  player.getEntityAttribute(net.minecraft.SharedMonsterAttributes.movementSpeed);
          net.minecraft.AttributeModifier modifier = attribute.getModifier(LEGS_SPEED_UUID);
          if (bonus > 0.0D)
          {
              if (modifier != null && modifier.getAmount() != bonus)
              {
                  attribute.removeModifier(modifier);
                  modifier = null;
              }
              if (modifier == null)
              {
                  attribute.applyModifier(new net.minecraft.AttributeModifier(
                          LEGS_SPEED_UUID, "ProjectE leggings speed", bonus, 1));
              }
          }
          else if (modifier != null)
          {
              attribute.removeModifier(modifier);
          }
          // Flight: MITE's flying speed comes from capabilities.flySpeed, not
          // the movementSpeed attribute, so scale it separately. The original
          // fly speed is captured once per player and restored when the
          // leggings are taken off.
          String flyKey = player.getEntityName();
          Float baseFly = LEGS_BASE_FLY_SPEED.get(flyKey);
          if (baseFly == null)
          {
              baseFly = player.capabilities.getFlySpeed();
              LEGS_BASE_FLY_SPEED.put(flyKey, baseFly);
          }
          player.capabilities.setFlySpeed(bonus > 0.0D ? baseFly * (1.0F + (float) bonus) : baseFly);

          if (player.ticksExisted % 40 == 0)
          {
              System.out.println("[ProjectE] legs-speed: bonus=" + bonus
                      + " base=" + attribute.getBaseValue()
                      + " value=" + attribute.getAttributeValue()
                      + " applied=" + (attribute.getModifier(LEGS_SPEED_UUID) != null)
                      + " armor=" + armorSlotName(player, 0) + "/" + armorSlotName(player, 1)
                      + "/" + armorSlotName(player, 2) + "/" + armorSlotName(player, 3)
                      + " fly=" + player.capabilities.getFlySpeed());
          }
      }

      private static String armorSlotName(net.minecraft.EntityPlayer player, int slot)
      {
          net.minecraft.ItemStack stack = player.inventory.armorInventory[slot];
          return stack == null || stack.getItem() == null ? "-" : stack.getItem().getClass().getSimpleName();
      }

      private static void updateKnockbackResistance(net.minecraft.EntityPlayer player)
      {
          if (player.worldObj.isRemote)
          {
              return;
          }
          boolean wearing = moze_intel.projecte.gameObjs.items.armor.RMArmor.hasAnyPiece(player)
                  || moze_intel.projecte.gameObjs.items.armor.GemArmorBase.hasAnyPiece(player);
          net.minecraft.AttributeInstance attribute =
                  player.getEntityAttribute(net.minecraft.SharedMonsterAttributes.knockbackResistance);
          if (wearing)
          {
              if (attribute.getModifier(KNOCKBACK_RESISTANCE_UUID) == null)
              {
                  attribute.applyModifier(KNOCKBACK_RESISTANCE_MODIFIER);
              }
          }
          else if (attribute.getModifier(KNOCKBACK_RESISTANCE_UUID) != null)
          {
              attribute.removeModifier(KNOCKBACK_RESISTANCE_MODIFIER);
          }
      }

      /**
       * Carried effects that should only ever apply once per player, even if
       * the player carries several copies (the pedestal functions are separate
       * and may stack across multiple pedestals).
       */
      private static boolean isUniqueCarriedEffect(net.minecraft.Item item)
      {
          return item instanceof moze_intel.projecte.gameObjs.items.rings.SoulStone
                  || item instanceof moze_intel.projecte.gameObjs.items.rings.LifeStone
                  || item instanceof moze_intel.projecte.gameObjs.items.rings.BodyStone
                  || item instanceof moze_intel.projecte.gameObjs.items.rings.MindStone
                  || item instanceof moze_intel.projecte.gameObjs.items.rings.BlackHoleBand
                  || item instanceof moze_intel.projecte.gameObjs.items.rings.Ignition
                  || item instanceof moze_intel.projecte.gameObjs.items.rings.Zero
                  || item instanceof moze_intel.projecte.gameObjs.items.rings.Arcana
                  || item instanceof moze_intel.projecte.gameObjs.items.rings.ArchangelSmite
                  || item instanceof moze_intel.projecte.gameObjs.items.rings.HarvestGoddess
                  || item instanceof moze_intel.projecte.gameObjs.items.GemEternalDensity
                  || item instanceof moze_intel.projecte.gameObjs.items.RepairTalisman
                  || item instanceof moze_intel.projecte.gameObjs.items.MercurialEye
                  || item instanceof moze_intel.projecte.gameObjs.items.EvertideAmulet
                  || item instanceof moze_intel.projecte.gameObjs.items.VolcaniteAmulet;
      }

    /**
     * Optional CarryOn integration: forbid carrying the dark matter pedestal
     * (its tile is not an inventory, so carrying it breaks the GUI / item
     * slot). Everything is done reflectively with a dynamic proxy, so ProjectE
     * has no compile-time or load-time dependency on CarryOn: without the mod
     * the class lookup fails and the integration is simply skipped, keeping
     * the game booting normally.
     */
    private static void registerCarryOnPluginIfPresent() {
        try {
            Class<?> pluginInterface = Class.forName("tschipp.carryon.api.CarryOnPlugin");
            Class<?> loaderClass = Class.forName("tschipp.carryon.api.CarryOnPluginLoader");
            Object plugin = java.lang.reflect.Proxy.newProxyInstance(
                    pluginInterface.getClassLoader(),
                    new Class<?>[]{pluginInterface},
                    (proxy, method, args) -> {
                        if (method.getDeclaringClass() == Object.class)
                        {
                            switch (method.getName())
                            {
                                case "toString": return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
                                case "hashCode": return System.identityHashCode(proxy);
                                case "equals": return proxy == args[0];
                                default: return null;
                            }
                        }
                        if (method.getName().equals("denyCarryBlock"))
                        {
                            // denyCarryBlock(EntityPlayer, Block, int meta)
                            return args[1] instanceof moze_intel.projecte.gameObjs.blocks.Pedestal;
                        }
                        if (method.getReturnType() == boolean.class)
                        {
                            return Boolean.FALSE; // abstain on everything else
                        }
                        return null;
                    });
            loaderClass.getMethod("register", pluginInterface).invoke(null, plugin);
            System.out.println("[ProjectE] CarryOn detected - pedestal is not carryable");
        } catch (Throwable t) {
            // CarryOn not installed (NoClassDefFoundError / ClassNotFoundException)
            // - the integration is optional, keep the game booting.
        }
    }

    private void onServerStarting(MinecraftServer server) {
        cpw.mods.fml.common.event.FMLServerStartingEvent event = new cpw.mods.fml.common.event.FMLServerStartingEvent(server);
        // Integrated servers already finalized at client startGame; dedicated
        // servers need it here (after every mod's main entrypoint).
        finalizeRecipeRegistration();
        PECore.instance.serverStarting(event);
        for (net.minecraft.ICommand command : event.getRegisteredCommands()) {
            server.getCommandManager().getCommands().put(command.getCommandName(), command);
        }
    }

    /**
     * Constructs CraftingManager (which fires FML's registry events once) and
     * registers any recipes still queued. Must run after every mod's main
     * entrypoint so late-loading mods (uncannybaubles etc.) receive the
     * ItemRegistryEvent and can register their items.
     */
    /**
     * Constructs CraftingManager (which fires FML's registry events once) and
     * registers any recipes still queued. On the client this is invoked at the
     * very start of Minecraft.startGame (ProjectEStartGameMixin) - after every
     * mod's main entrypoint has run, but before MITE's item atlas assigns each
     * item its icon. That ordering matters: mods that construct their items
     * inside the ItemRegistryEvent (e.g. UncannyBaubles) must exist in
     * Item.itemsList before the atlas pass, or their itemIcon stays null and
     * they render as the missing-texture purple/black blocks. The manylib
     * startGame-RETURN handler is kept as a guarded fallback.
     */
    public static void finalizeRecipeRegistration() {
        if (recipeRegistrationDone) {
            return;
        }
        recipeRegistrationDone = true;
        net.minecraft.CraftingManager.getInstance();
        cpw.mods.fml.common.registry.GameRegistry.drainAndRegisterRecipes();
        // CraftingManager's constructor posts FML's ItemRegistryEvent /
        // BlockRegistryEvent exactly once, whenever the class first loads -
        // which can happen during an early-loading mod's init (e.g. EMI),
        // BEFORE mods that load later (uncannybaubles, waila) register their
        // listeners, so their items never register. Re-posting the events now
        // (after every mod's main entrypoint) gives those mods their chance.
        // Registrars that already handled the first posting simply re-set the
        // namespace/creative tab of their existing items - harmless.
        // Re-posting the registry events lets mods whose listeners registered
        // after CraftingManager was first constructed receive their items.
        // Some registrars are not idempotent (they call Item.setNamespace on
        // an already-locked WriteLockField and throw), so guard each pass to
        // make sure a single noisy registrar can never abort Minecraft's
        // startGame sequence.
        try
        {
            net.xiaoyu233.fml.reload.event.MITEEvents.MITE_EVENT_BUS.post(
                    new net.xiaoyu233.fml.reload.event.ItemRegistryEvent());
        }
        catch (Throwable t)
        {
            System.out.println("[ProjectE] ItemRegistryEvent re-post failed (ignored): " + t);
        }
        try
        {
            net.xiaoyu233.fml.reload.event.MITEEvents.MITE_EVENT_BUS.post(
                    new net.xiaoyu233.fml.reload.event.BlockRegistryEvent());
        }
        catch (Throwable t)
        {
            System.out.println("[ProjectE] BlockRegistryEvent re-post failed (ignored): " + t);
        }
    }
}
