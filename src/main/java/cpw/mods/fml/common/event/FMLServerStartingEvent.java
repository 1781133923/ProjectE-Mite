package cpw.mods.fml.common.event;

import net.minecraft.ICommand;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class FMLServerStartingEvent {
    private final MinecraftServer server;
    private final List<ICommand> commands = new ArrayList<>();

    public FMLServerStartingEvent(MinecraftServer server) {
        this.server = server;
    }

    public void registerServerCommand(ICommand command) {
        this.commands.add(command);
    }

    public List<ICommand> getRegisteredCommands() {
        return this.commands;
    }

    public MinecraftServer getServer() {
        return this.server;
    }
}
