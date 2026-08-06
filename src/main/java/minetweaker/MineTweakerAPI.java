package minetweaker;

public class MineTweakerAPI {
    public static void logError(String message) {
    }

    public static void apply(IUndoableAction action) {
        try {
            action.apply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerClass(Class<?> clazz) {
    }
}
