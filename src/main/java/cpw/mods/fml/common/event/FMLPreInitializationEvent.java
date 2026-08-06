package cpw.mods.fml.common.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class FMLPreInitializationEvent {
    private final File modConfigurationDirectory;
    private final File sourceFile;

    public FMLPreInitializationEvent(File modConfigurationDirectory) {
        this(modConfigurationDirectory, null);
    }

    public FMLPreInitializationEvent(File modConfigurationDirectory, File sourceFile) {
        this.modConfigurationDirectory = modConfigurationDirectory;
        this.sourceFile = sourceFile;
    }

    public File getModConfigurationDirectory() {
        return this.modConfigurationDirectory;
    }

    public File getSourceFile() {
        return this.sourceFile;
    }

    public Logger getModLog() {
        return LogManager.getLogger("ProjectE");
    }

    public Object getModMetadata() {
        return null;
    }

    public String getFMLVersion() {
        return "3.4.1";
    }
}
