package cpw.mods.fml.client.registry;

import net.minecraft.Entity;
import net.minecraft.Render;
import net.xiaoyu233.fml.reload.utils.IdUtil;

import java.util.ArrayList;
import java.util.List;

public class RenderingRegistry {
    private static final List<EntityRendererInfo> ENTITY_RENDERERS = new ArrayList<>();

    public static int getNextAvailableRenderId() {
        return IdUtil.getNextRenderType();
    }

    public static void registerEntityRenderingHandler(Class<? extends Entity> entityClass, Render renderer) {
        ENTITY_RENDERERS.add(new EntityRendererInfo(entityClass, renderer));
    }

    public static void registerEntityRenderingHandler(Entity entity, Render renderer) {
        ENTITY_RENDERERS.add(new EntityRendererInfo(entity.getClass(), renderer));
    }

    public static List<EntityRendererInfo> getEntityRenderers() {
        return ENTITY_RENDERERS;
    }

    public record EntityRendererInfo(Class<? extends Entity> entityClass, Render renderer) {
    }
}
