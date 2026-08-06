package cpw.mods.fml.common;

import com.google.common.eventbus.EventBus;
import net.xiaoyu233.fml.FishModLoader;
import net.xiaoyu233.fml.reload.event.MITEEvents;
import cpw.mods.fml.relauncher.Side;

public class FMLCommonHandler {
    private static final FMLCommonHandler INSTANCE = new FMLCommonHandler();

    public static FMLCommonHandler instance() {
        return INSTANCE;
    }

    public EventBus bus() {
        return MITEEvents.MITE_EVENT_BUS;
    }

    public Side getSide() {
        return FishModLoader.isServer() ? Side.SERVER : Side.CLIENT;
    }

    public Side getEffectiveSide() {
        return getSide();
    }

    public void raiseException(Throwable exception, String message, boolean stopEverything) {
        throw new RuntimeException(message, exception);
    }

    public void fireSidedRegistryEvents() {
    }

    public void showGuiScreen(Object gui) {
        if (gui instanceof net.minecraft.GuiScreen) {
            net.minecraft.Minecraft.getMinecraft().displayGuiScreen((net.minecraft.GuiScreen) gui);
        }
    }
}
