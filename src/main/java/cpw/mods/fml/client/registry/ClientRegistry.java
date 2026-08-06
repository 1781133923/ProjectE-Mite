package cpw.mods.fml.client.registry;

import cpw.mods.fml.client.registry.RenderingRegistry.EntityRendererInfo;
import net.minecraft.KeyBinding;
import net.minecraft.TileEntity;
import net.minecraft.TileEntitySpecialRenderer;

import java.util.ArrayList;
import java.util.List;

public class ClientRegistry {
    private static final List<KeyBinding> KEY_BINDINGS = new ArrayList<>();
    private static final List<TileEntityRendererInfo> TILE_ENTITY_RENDERERS = new ArrayList<>();

    public static void registerKeyBinding(KeyBinding keyBinding) {
        KEY_BINDINGS.add(keyBinding);
    }

    public static void registerTileEntitySpecialRenderer(Class<? extends TileEntity> tileEntityClass, TileEntitySpecialRenderer renderer) {
        TILE_ENTITY_RENDERERS.add(new TileEntityRendererInfo(tileEntityClass, renderer));
    }

    public static void bindTileEntitySpecialRenderer(Class<? extends TileEntity> tileEntityClass, TileEntitySpecialRenderer renderer) {
        registerTileEntitySpecialRenderer(tileEntityClass, renderer);
    }

    public static List<KeyBinding> getKeyBindings() {
        return KEY_BINDINGS;
    }

    public static List<TileEntityRendererInfo> getTileEntityRenderers() {
        return TILE_ENTITY_RENDERERS;
    }

    public record TileEntityRendererInfo(Class<? extends TileEntity> clazz, TileEntitySpecialRenderer renderer) {
    }
}
