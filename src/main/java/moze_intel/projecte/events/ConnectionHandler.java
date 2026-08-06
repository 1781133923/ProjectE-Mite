package moze_intel.projecte.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import moze_intel.projecte.compat.ExtendedProperties;
import moze_intel.projecte.handlers.PlayerChecks;
import moze_intel.projecte.handlers.PlayerTimers;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.utils.PELogger;
import net.minecraft.ServerPlayer;

public class ConnectionHandler
{
	@SubscribeEvent
	public void playerConnect(PlayerLoggedInEvent event)
	{
		PacketHandler.sendFragmentedEmcPacket((ServerPlayer) event.player);
		// 更新检查已按需求移除：不再向客户端发送检查更新请求。

		PlayerTimers.registerPlayer(event.player);
		
	}

	@SubscribeEvent
	public void playerDisconnect(PlayerEvent.PlayerLoggedOutEvent event)
	{
		PlayerTimers.removePlayer(event.player);
		// Drop this player's in-memory transmutation/bag props so switching
		// saves (or servers) inside one game session cannot leak knowledge,
		// EMC or bag contents into the next world. The data was already
		// written to this world's players/<name>.dat before this event.
		ExtendedProperties.remove(event.player);
		PELogger.logInfo("Removing " + event.player.getCommandSenderName() + " from scheduled timers: Player disconnected.");
		PlayerChecks.removePlayerFromLists(((ServerPlayer) event.player));
	}

}
