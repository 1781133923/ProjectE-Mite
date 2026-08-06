package cpw.mods.fml.common;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FMLLog {
    private static final Logger LOGGER = LogManager.getLogger("FML");

    public static void log(Level level, String format, Object... data) {
        LOGGER.log(level, String.format(format, data));
    }

    public static void info(String format, Object... data) {
        LOGGER.info(String.format(format, data));
    }

    public static void warning(String format, Object... data) {
        LOGGER.warn(String.format(format, data));
    }

    public static void severe(String format, Object... data) {
        LOGGER.error(String.format(format, data));
    }
}
