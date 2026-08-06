package cpw.mods.fml.common;

public enum LoaderState {
    NOINIT,
    LOADING,
    CONSTRUCTING,
    PREINITIALIZATION,
    INITIALIZATION,
    POSTINITIALIZATION,
    AVAILABLE,
    SERVER_STARTING,
    SERVER_STARTED,
    SERVER_STOPPING,
    SERVER_STOPPED,
    ERRORED;

    public enum ModState {
        LOADED,
        DISABLED,
        ERRORED
    }
}
