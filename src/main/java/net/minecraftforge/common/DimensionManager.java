package net.minecraftforge.common;

import net.minecraft.server.MinecraftServer;

import java.io.File;

public class DimensionManager {
    public static File getCurrentSaveRootDirectory() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.worldServers == null || server.worldServers.length == 0) {
            return null;
        }
        return new File(server.worldServers[0].getSaveHandler().getWorldDirectoryName());
    }
}
