package moze_intel.projecte.compat;

import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

/**
 * Bridges the FishModLoader-native player events onto the Forge-shim event bus
 * that ProjectE's own event handlers listen on.
 */
public final class FMLEventBridge {
    @Subscribe
    public void onPlayerLoggedIn(net.xiaoyu233.fml.reload.event.PlayerLoggedInEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) {
            return;
        }
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(player));
        MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(player, player.worldObj));
    }
}
