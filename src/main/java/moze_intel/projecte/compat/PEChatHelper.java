package moze_intel.projecte.compat;

import net.minecraft.EntityPlayer;
import net.minecraft.IChatComponent;

public final class PEChatHelper {
    private PEChatHelper() {
    }

    public static void send(EntityPlayer player, IChatComponent component) {
        if (player == null || component == null) {
            return;
        }
        player.addChatMessage(component.getFormattedText());
    }

    public static void send(EntityPlayer player, String message) {
        if (player == null || message == null) {
            return;
        }
        player.addChatMessage(message);
    }
}
