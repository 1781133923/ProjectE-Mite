package cpw.mods.fml.common.registry;

import net.minecraft.Entity;

import java.util.ArrayList;
import java.util.List;

public class EntityRegistry {
    private static final List<EntityRegistration> QUEUE = new ArrayList<>();

    public static void registerModEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
        QUEUE.add(new EntityRegistration(entityClass, entityName, id, trackingRange, updateFrequency, sendsVelocityUpdates, -1, -1));
    }

    public static void registerModEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int eggColor1, int eggColor2) {
        QUEUE.add(new EntityRegistration(entityClass, entityName, id, trackingRange, updateFrequency, sendsVelocityUpdates, eggColor1, eggColor2));
    }

    public static List<EntityRegistration> drainQueue() {
        List<EntityRegistration> result = new ArrayList<>(QUEUE);
        QUEUE.clear();
        return result;
    }

    public record EntityRegistration(Class<? extends Entity> entityClass, String entityName, int id,
                                      int trackingRange, int updateFrequency, boolean sendsVelocityUpdates,
                                      int eggColor1, int eggColor2) {
    }
}
