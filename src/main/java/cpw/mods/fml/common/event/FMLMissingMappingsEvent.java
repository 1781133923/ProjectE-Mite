package cpw.mods.fml.common.event;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.Block;
import net.minecraft.Item;

import java.util.ArrayList;
import java.util.List;

public class FMLMissingMappingsEvent {
    private final List<MissingMapping> mappings = new ArrayList<>();

    public List<MissingMapping> get() {
        return this.mappings;
    }

    public Iterable<MissingMapping> getAllMappings() {
        return this.mappings;
    }

    public void addMapping(String name, GameRegistry.Type type) {
        this.mappings.add(new MissingMapping(name, type));
    }

    public static class MissingMapping {
        public final String name;
        public final GameRegistry.Type type;

        public MissingMapping(String name, GameRegistry.Type type) {
            this.name = name;
            this.type = type;
        }

        public void remap(Item item) {
            GameRegistry.addAlias(item, this.name);
        }

        public void remap(Block block) {
            GameRegistry.addAlias(block, this.name);
        }

        public void ignore() {
        }
    }
}
