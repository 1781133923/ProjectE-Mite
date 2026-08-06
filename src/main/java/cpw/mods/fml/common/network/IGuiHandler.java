package cpw.mods.fml.common.network;

import net.minecraft.EntityPlayer;
import net.minecraft.World;

public interface IGuiHandler {
    Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z);

    Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z);
}
