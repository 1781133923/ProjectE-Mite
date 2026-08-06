package codechicken.nei;

public class NEIClientUtils {
    public static String translate(String key) {
        return net.minecraft.StatCollector.translateToLocal(key);
    }
}
