package moze_intel.projecte.events;

import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import moze_intel.projecte.handlers.PlayerChecks;
import moze_intel.projecte.handlers.PlayerTimers;
import net.minecraft.ServerPlayer;

public class TickEvents
{
	@Subscribe
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
		{
			PlayerTimers.update();
		}
	}

	@Subscribe
	public void playerTick(TickEvent.PlayerTickEvent event)
	{
		// RIC fires the player tick on both sides; the tick event stub always
		// reports SERVER, so gate on the actual player type.
		if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer)
		{
			PlayerChecks.update(((ServerPlayer) event.player));
		}
	}
}
