package moze_intel.projecte.handlers;

import com.google.common.collect.Maps;
import net.minecraft.EntityPlayer;

import java.util.LinkedHashMap;

public final class PlayerTimers
{
	private static final LinkedHashMap<String, TimerSet> MAP = Maps.newLinkedHashMap();

	public static void update()
	{
		for (TimerSet timers : MAP.values())
		{
			if (timers.repair.shouldUpdate)
			{
				if (timers.repair.tickCount < 19)
				{
					timers.repair.tickCount++;
				}

				timers.repair.shouldUpdate = false;
			}

			if (timers.heal.shouldUpdate)
			{
				if (timers.heal.tickCount < 19)
				{
					timers.heal.tickCount++;
				}

				timers.heal.shouldUpdate = false;
			}

			if (timers.feed.shouldUpdate)
			{
				if (timers.feed.tickCount < 19)
				{
					timers.feed.tickCount++;
				}

				timers.feed.shouldUpdate = false;
			}

			// Body Stone feeds one 1/2-shank bite every 80 ticks (4 s).
			if (timers.bodyFeed.shouldUpdate)
			{
				if (timers.bodyFeed.tickCount < 79)
				{
					timers.bodyFeed.tickCount++;
				}

				timers.bodyFeed.shouldUpdate = false;
			}

			// Life Stone feeds one 1/2-shank bite every 40 ticks (2 s).
			if (timers.lifeFeed.shouldUpdate)
			{
				if (timers.lifeFeed.tickCount < 39)
				{
					timers.lifeFeed.tickCount++;
				}

				timers.lifeFeed.shouldUpdate = false;
			}

			// Gem chestplate feeds one 1/2-shank bite every 80 ticks (4 s).
			if (timers.gemFeed.shouldUpdate)
			{
				if (timers.gemFeed.tickCount < 79)
				{
					timers.gemFeed.tickCount++;
				}

				timers.gemFeed.shouldUpdate = false;
			}

			// Gem helmet heals twice as fast as the Soul stone: one heart
			// every 20 ticks instead of half a heart.
			if (timers.gemHeal.shouldUpdate)
			{
				if (timers.gemHeal.tickCount < 19)
				{
					timers.gemHeal.tickCount++;
				}

				timers.gemHeal.shouldUpdate = false;
			}
		}
	}

	public static void registerPlayer(EntityPlayer player)
	{
		MAP.put(player.getCommandSenderName(), new TimerSet());
	}

	public static void removePlayer(EntityPlayer player)
	{
		MAP.remove(player.getCommandSenderName());
	}

	public static void activateRepair(EntityPlayer player)
	{
		MAP.get(player.getCommandSenderName()).repair.shouldUpdate = true;
	}

	public static void activateHeal(EntityPlayer player)
	{
		MAP.get(player.getCommandSenderName()).heal.shouldUpdate = true;
	}

	public static void activateFeed(EntityPlayer player)
	{
		MAP.get(player.getCommandSenderName()).feed.shouldUpdate = true;
	}

	public static void activateBodyFeed(EntityPlayer player)
	{
		MAP.get(player.getCommandSenderName()).bodyFeed.shouldUpdate = true;
	}

	public static void activateLifeFeed(EntityPlayer player)
	{
		MAP.get(player.getCommandSenderName()).lifeFeed.shouldUpdate = true;
	}

	public static void activateGemFeed(EntityPlayer player)
	{
		MAP.get(player.getCommandSenderName()).gemFeed.shouldUpdate = true;
	}

	public static void activateGemHeal(EntityPlayer player)
	{
		MAP.get(player.getCommandSenderName()).gemHeal.shouldUpdate = true;
	}

	public static boolean canRepair(EntityPlayer player)
	{
		Timer timer = MAP.get(player.getCommandSenderName()).repair;

		if (timer.tickCount >= 19)
		{
			timer.tickCount = 0;
			timer.shouldUpdate = false;
			return true;
		}

		return false;
	}

	public static boolean canHeal(EntityPlayer player)
	{
		Timer timer = MAP.get(player.getCommandSenderName()).heal;

		if (timer.tickCount >= 19)
		{
			timer.tickCount = 0;
			timer.shouldUpdate = false;
			return true;
		}

		return false;
	}

	public static boolean canFeed(EntityPlayer player)
	{
		Timer timer = MAP.get(player.getCommandSenderName()).feed;

		if (timer.tickCount >= 19)
		{
			timer.tickCount = 0;
			timer.shouldUpdate = false;
			return true;
		}

		return false;
	}

	public static boolean canBodyFeed(EntityPlayer player)
	{
		Timer timer = MAP.get(player.getCommandSenderName()).bodyFeed;

		if (timer.tickCount >= 79)
		{
			timer.tickCount = 0;
			timer.shouldUpdate = false;
			return true;
		}

		return false;
	}

	public static boolean canLifeFeed(EntityPlayer player)
	{
		Timer timer = MAP.get(player.getCommandSenderName()).lifeFeed;

		if (timer.tickCount >= 39)
		{
			timer.tickCount = 0;
			timer.shouldUpdate = false;
			return true;
		}

		return false;
	}

	public static boolean canGemFeed(EntityPlayer player)
	{
		Timer timer = MAP.get(player.getCommandSenderName()).gemFeed;

		if (timer.tickCount >= 79)
		{
			timer.tickCount = 0;
			timer.shouldUpdate = false;
			return true;
		}

		return false;
	}

	public static boolean canGemHeal(EntityPlayer player)
	{
		Timer timer = MAP.get(player.getCommandSenderName()).gemHeal;

		if (timer.tickCount >= 19)
		{
			timer.tickCount = 0;
			timer.shouldUpdate = false;
			return true;
		}

		return false;
	}

	private static class TimerSet
	{
		public Timer repair;
		public Timer heal;
		public Timer feed;
		public Timer bodyFeed;
		public Timer lifeFeed;
		public Timer gemFeed;
		public Timer gemHeal;

		public TimerSet()
		{
			repair = new Timer();
			heal = new Timer();
			feed = new Timer();
			bodyFeed = new Timer();
			lifeFeed = new Timer();
			gemFeed = new Timer();
			gemHeal = new Timer();
		}
	}

	private static class Timer
	{
		public short tickCount;
		public boolean shouldUpdate;

		public Timer()
		{
			tickCount = 0;
			shouldUpdate = false;
		}

		@Override
		public String toString()
		{
			return "TICKS: " + tickCount + "\n" + "ACTIVE: " + shouldUpdate;
		}
	}
}
