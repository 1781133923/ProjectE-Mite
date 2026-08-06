package cpw.mods.fml.common;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Loader {
    private static final Loader INSTANCE = new Loader();
    private static final List<ModContainer> MODS = new java.util.concurrent.CopyOnWriteArrayList<>();

    public static Loader instance() {
        return INSTANCE;
    }

    public static void registerModContainer(ModContainer container) {
        MODS.add(container);
    }

    public static boolean isModLoaded(String modId) {
        return instance().isModLoadedInternal(modId);
    }

    public boolean isModLoadedInternal(String modId) {
        for (ModContainer mod : MODS) {
            if (mod.getModId().equalsIgnoreCase(modId)) {
                return true;
            }
        }
        return false;
    }

    public List<ModContainer> getActiveModList() {
        return Collections.unmodifiableList(MODS);
    }

    public ModContainer getIndexedModList() {
        return null;
    }

    public Object getModState() {
        return LoaderState.AVAILABLE;
    }

    public Map<String, ModContainer> getIndexedMods() {
        return Collections.emptyMap();
    }

    public ModContainer activeModContainer() {
        return new ModContainer() {
            @Override
            public String getModId() {
                return "projecte";
            }

            @Override
            public String getName() {
                return "ProjectE";
            }

            @Override
            public String getVersion() {
                return "0.1.0";
            }
        };
    }

    public boolean isInState(LoaderState state) {
        return true;
    }

    public boolean hasReachedState(LoaderState state) {
        return true;
    }
}
