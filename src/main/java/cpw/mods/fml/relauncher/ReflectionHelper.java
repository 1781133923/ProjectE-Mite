package cpw.mods.fml.relauncher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionHelper {
    public static <T, E> T getPrivateValue(Class<? super E> classToAccess, E instance, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = classToAccess.getDeclaredField(fieldName);
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                T result = (T) field.get(instance);
                return result;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        throw new RuntimeException("Unable to find field in " + classToAccess + " from names " + String.join(",", fieldNames));
    }

    public static <T, E> void setPrivateValue(Class<? super T> classToAccess, T instance, E value, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = classToAccess.getDeclaredField(fieldName);
                field.setAccessible(true);
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                field.set(instance, value);
                return;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        throw new RuntimeException("Unable to set field in " + classToAccess + " from names " + String.join(",", fieldNames));
    }
}
