package codechicken.nei;

public class NEClientUtils {
    public static String translate(String key) {
        return net.minecraft.StatCollector.translateToLocal(key);
    }
}
