package moze_intel.projecte.proxies;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import moze_intel.projecte.events.PlayerRender;
import moze_intel.projecte.events.ToolTipEvent;
import moze_intel.projecte.events.TransmutationRenderingEvent;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.entity.EntityFireProjectile;
import moze_intel.projecte.gameObjs.entity.EntityLavaProjectile;
import moze_intel.projecte.gameObjs.entity.EntityLensProjectile;
import moze_intel.projecte.gameObjs.entity.EntityLootBall;
import moze_intel.projecte.gameObjs.entity.EntityMobRandomizer;
import moze_intel.projecte.gameObjs.entity.EntityNovaCataclysmPrimed;
import moze_intel.projecte.gameObjs.entity.EntityNovaCatalystPrimed;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.entity.EntityWaterProjectile;
import moze_intel.projecte.gameObjs.tiles.AlchChestTile;
import moze_intel.projecte.gameObjs.tiles.CondenserMK2Tile;
import moze_intel.projecte.gameObjs.tiles.CondenserTile;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.manual.ManualPageHandler;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.KeyPressPKT;
import moze_intel.projecte.playerData.AlchBagProps;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.playerData.TransmutationProps;
import moze_intel.projecte.rendering.ChestItemRenderer;
import moze_intel.projecte.rendering.ChestRenderer;
import moze_intel.projecte.rendering.CondenserItemRenderer;
import moze_intel.projecte.rendering.CondenserMK2ItemRenderer;
import moze_intel.projecte.rendering.CondenserMK2Renderer;
import moze_intel.projecte.rendering.CondenserRenderer;
import moze_intel.projecte.rendering.NovaCataclysmRenderer;
import moze_intel.projecte.rendering.NovaCatalystRenderer;
import moze_intel.projecte.rendering.PEProjectileRenderer;
import moze_intel.projecte.rendering.PedestalItemRenderer;
import moze_intel.projecte.rendering.PedestalRenderer;
import moze_intel.projecte.utils.ClientKeyHelper;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy implements IProxy
{
	// These three following methods are here to prevent a strange crash in the dedicated server whenever packets are received
	// and the wrapped methods are called directly.

	@Override
	public void clearClientKnowledge()
	{
		Transmutation.clearKnowledge(FMLClientHandler.instance().getClientPlayerEntity());
	}

	@Override
	public TransmutationProps getClientTransmutationProps()
	{
		return TransmutationProps.getDataFor(FMLClientHandler.instance().getClientPlayerEntity());
	}

	@Override
	public AlchBagProps getClientBagProps()
	{
		return AlchBagProps.getDataFor(FMLClientHandler.instance().getClientPlayerEntity());
	}

	@Override
	public void registerKeyBinds()
	{
		// The bindings themselves are appended to GameSettings by
		// ProjectEKeybindingsMixin (same approach as NewShop); this only makes
		// sure they exist before the tick starts polling them.
		ClientKeyHelper.ensureBindings();

		// FishModLoader/MITE do not fire Forge's KeyInputEvent, so poll the
		// ProjectE keybindings every client tick and forward presses to the server.
		moddedmite.rustedironcore.api.event.Handlers.Tick.register(new moddedmite.rustedironcore.api.event.listener.ITickListener()
		{
			@Override
			public void onClientTick(net.minecraft.Minecraft mc)
			{
				if (ClientKeyHelper.mcToPe == null || mc == null || mc.thePlayer == null)
				{
					return;
				}
				for (net.minecraft.KeyBinding binding : ClientKeyHelper.mcToPe.keySet())
				{
					if (binding.isPressed())
					{
						PacketHandler.sendToServer(new KeyPressPKT(ClientKeyHelper.mcToPe.get(binding)));
					}
				}
			}
		});
	}

	/**
	 * Called from ProjectE.onInitialize (before Minecraft is constructed).
	 * MITE's RenderManager posts EntityRendererRegistryEvent during startGame,
	 * which is earlier than the deferred manylib init handler that used to
	 * register these renderers - so the event fired with no subscriber and the
	 * projectile renderers never reached RenderManager (invisible orbs).
	 * Registering the subscriber and the renderers here guarantees the loader
	 * injects them with setRenderManager already called.
	 */
	public void registerEntityRenderersEarly()
	{
		FMLCommonHandler.instance().bus().register(new Object() {
			@com.google.common.eventbus.Subscribe
			public void onEntityRendererRegister(net.xiaoyu233.fml.reload.event.EntityRendererRegistryEvent event) {
				for (RenderingRegistry.EntityRendererInfo info : RenderingRegistry.getEntityRenderers()) {
					event.register(info.entityClass(), info.renderer());
				}
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityWaterProjectile.class, new PEProjectileRenderer("items/entities/water_orb.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityLavaProjectile.class, new PEProjectileRenderer("items/entities/lava_orb.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityLootBall.class, new PEProjectileRenderer("items/entities/loot_ball.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityMobRandomizer.class, new PEProjectileRenderer("items/entities/randomizer.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityLensProjectile.class, new PEProjectileRenderer("items/entities/lens_explosive.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityNovaCatalystPrimed.class, new NovaCatalystRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityNovaCataclysmPrimed.class, new NovaCataclysmRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityFireProjectile.class, new PEProjectileRenderer("items/entities/fireball.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntitySWRGProjectile.class, new PEProjectileRenderer("items/entities/lightning.png"));
	}

	@Override
	public void registerRenderers() 
	{
		//Items
		MinecraftForgeClient.registerItemRenderer(net.minecraft.Item.getItem(ObjHandler.alchChest), new ChestItemRenderer());
		MinecraftForgeClient.registerItemRenderer(net.minecraft.Item.getItem(ObjHandler.condenser), new CondenserItemRenderer());
		MinecraftForgeClient.registerItemRenderer(net.minecraft.Item.getItem(ObjHandler.condenserMk2), new CondenserMK2ItemRenderer());
		MinecraftForgeClient.registerItemRenderer(net.minecraft.Item.getItem(ObjHandler.dmPedestal), new PedestalItemRenderer());

		//Blocks
		ClientRegistry.bindTileEntitySpecialRenderer(AlchChestTile.class, new ChestRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(CondenserTile.class, new CondenserRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(CondenserMK2Tile.class, new CondenserMK2Renderer());
		ClientRegistry.bindTileEntitySpecialRenderer(DMPedestalTile.class, new PedestalRenderer());
	}
	
	@Override
	public void registerClientOnlyEvents() 
	{
		MinecraftForge.EVENT_BUS.register(new ToolTipEvent());
		MinecraftForge.EVENT_BUS.register(new TransmutationRenderingEvent());

		// MITE fires TileEntityRendererRegisterEvent when its renderer is
		// initialised; forward the TESRs collected by ClientRegistry into it.
		FMLCommonHandler.instance().bus().register(new Object() {
			@com.google.common.eventbus.Subscribe
			public void onTileEntityRendererRegister(net.xiaoyu233.fml.reload.event.TileEntityRendererRegisterEvent event) {
				for (ClientRegistry.TileEntityRendererInfo info : ClientRegistry.getTileEntityRenderers()) {
					event.register(info.clazz(), info.renderer());
				}
			}
		});

		// Fallback: the FML renderer-register event can fire before this
		// subscriber is registered (TileEntityRenderer is initialised early on
		// the client). Ensure the TESRs land in MITE's renderer map once the
		// renderer instance exists.
		moddedmite.rustedironcore.api.event.Handlers.Tick.register(new moddedmite.rustedironcore.api.event.listener.ITickListener() {
			private boolean done = false;

			@Override
			public void onClientTick(net.minecraft.Minecraft mc) {
				if (done) {
					return;
				}
				done = true;
				try {
					Class<?> rendererClass = Class.forName("net.minecraft.TileEntityRenderer");
					java.lang.reflect.Field instanceField = rendererClass.getDeclaredField("instance");
					instanceField.setAccessible(true);
					Object instance = instanceField.get(null);
					if (instance == null) {
						done = false;
						return;
					}
					java.lang.reflect.Field mapField = rendererClass.getDeclaredField("specialRendererMap");
					mapField.setAccessible(true);
					java.util.Map<Class<?>, Object> map =
							(java.util.Map<Class<?>, Object>) mapField.get(instance);
					for (ClientRegistry.TileEntityRendererInfo info : ClientRegistry.getTileEntityRenderers()) {
						map.put(info.clazz(), info.renderer());
					}
					// Same best-effort injection for entity renderers.
					Class<?> renderManagerClass = Class.forName("net.minecraft.RenderManager");
					java.lang.reflect.Field instanceField2 = renderManagerClass.getDeclaredField("instance");
					instanceField2.setAccessible(true);
					Object renderManager = instanceField2.get(null);
					if (renderManager != null) {
						java.lang.reflect.Field entityMapField = renderManagerClass.getDeclaredField("entityRenderMap");
						entityMapField.setAccessible(true);
						java.util.Map<Class<?>, Object> entityMap =
								(java.util.Map<Class<?>, Object>) entityMapField.get(renderManager);
						for (RenderingRegistry.EntityRendererInfo info : RenderingRegistry.getEntityRenderers()) {
							net.minecraft.Render renderer = info.renderer();
							// The loader only calls setRenderManager for
							// renderers registered through its event; the
							// fallback path must do it itself or RenderSnowball
							// NPEs while binding the item-atlas texture.
							renderer.setRenderManager((net.minecraft.RenderManager) renderManager);
							entityMap.put(info.entityClass(), renderer);
						}
					}
				} catch (Throwable t) {
					// Best-effort; the FML event path may already have handled it.
				}
			}
		});

		PlayerRender pr = new PlayerRender();
		MinecraftForge.EVENT_BUS.register(pr);
		FMLCommonHandler.instance().bus().register(pr);
	}

	@Override
	public void initializeManual()
	{
		ManualPageHandler.init();
	}

	@Override
	public EntityPlayer getClientPlayer()
	{
		return FMLClientHandler.instance().getClientPlayerEntity();
	}

	@Override
	public boolean isJumpPressed()
	{
		return FMLClientHandler.instance().getClient().gameSettings.keyBindJump.isPressed();
	}
}
