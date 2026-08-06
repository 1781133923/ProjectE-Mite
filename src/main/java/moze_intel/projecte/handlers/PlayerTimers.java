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

			// Body/Life stones feed 8x slower than the other feed sources:
			// one 1/2-shank bite every 160 ticks.
			if (timers.slowFeed.shouldUpdate)
			{
				if (timers.slowFeed.tickCount < 159)
				{
					timers.slowFeed.tickCount++;
				}

				timers.slowFeed.shouldUpdate = false;
			}

			// Gem chestplate feeds twice as fast as the Life stone: every 80
			// ticks instead of 160.
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

	public static void activateSlowFeed(EntityPlayer player)
	{
		MAP.get(player.getCommandSenderName()).slowFeed.shouldUpdate = true;
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

	public static boolean canSlowFeed(EntityPlayer player)
	{
		Timer timer = MAP.get(player.getCommandSenderName()).slowFeed;

		if (timer.tickCount >= 159)
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
		public Timer slowFeed;
		public Timer gemFeed;
		public Timer gemHeal;

		public TimerSet()
		{
			repair = new Timer();
			heal = new Timer();
			feed = new Timer();
			slowFeed = new Timer();
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
