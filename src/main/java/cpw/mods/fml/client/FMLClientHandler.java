package cpw.mods.fml.client;

import net.minecraft.Minecraft;
import net.minecraft.EntityPlayer;

public class FMLClientHandler {
    private static final FMLClientHandler INSTANCE = new FMLClientHandler();

    public static FMLClientHandler instance() {
        return INSTANCE;
    }

    public Minecraft getClient() {
        return Minecraft.getMinecraft();
    }

    public Object getClientPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    public EntityPlayer getClientPlayerEntity() {
        return Minecraft.getMinecraft().thePlayer;
    }

    public void displayGuiScreen(Object gui) {
        if (gui instanceof net.minecraft.GuiScreen) {
            Minecraft.getMinecraft().displayGuiScreen((net.minecraft.GuiScreen) gui);
        }
    }
}
