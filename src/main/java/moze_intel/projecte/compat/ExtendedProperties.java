package moze_intel.projecte.compat;

import net.minecraft.EntityPlayer;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stand-in for Forge's IExtendedEntityProperties attachment system.
 */
public final class ExtendedProperties {
    private static final Map<String, Map<String, IExtendedEntityProperties>> PROPERTIES = new HashMap<>();

    private ExtendedProperties() {
    }

    public static void register(EntityPlayer player, String name, IExtendedEntityProperties properties) {
        PROPERTIES.computeIfAbsent(keyFor(player), k -> new HashMap<>()).put(name, properties);
    }

    public static IExtendedEntityProperties get(EntityPlayer player, String name) {
        Map<String, IExtendedEntityProperties> map = PROPERTIES.get(keyFor(player));
        return map == null ? null : map.get(name);
    }

    public static Map<String, IExtendedEntityProperties> getForPlayer(EntityPlayer player) {
        return PROPERTIES.computeIfAbsent(keyFor(player), k -> new HashMap<>());
    }

    /**
     * Drops every in-memory property of one player. Called on disconnect so
     * the next world/server the player joins starts from its own player NBT
     * instead of stale data left over from the previous save.
     */
    public static void remove(EntityPlayer player) {
        if (player != null) {
            PROPERTIES.remove(keyFor(player));
        }
    }

    /**
     * Drops the whole in-memory property table. Called when the server stops;
     * player data has already been written to disk by then.
     */
    public static void clearAll() {
        PROPERTIES.clear();
    }

    private static String keyFor(EntityPlayer player) {
        if (player == null) {
            return "player:null";
        }
        // Key props by the player's name rather than the entity UUID. MITE's
        // respawn flow builds a fresh ServerPlayer whose UUID is random until
        // NBT is re-read, while the name stays stable across death, so UUID
        // keys would make PlayerEvents.cloneEvent write into a different map
        // entry than the original player's props (losing knowledge/EMC on
        // death). The name is also exactly how the player data file is keyed.
        return "player:" + player.getCommandSenderName();
    }
}
