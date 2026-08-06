package moze_intel.projecte.handlers;

import com.google.common.collect.Sets;


import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.items.IFireProtector;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.items.IStepAssister;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.IInventory;
import net.minecraft.ItemStack;

import java.util.Set;

public final class PlayerChecks
{
	private static final Set<ServerPlayer> swrgOverrides = Sets.newHashSet();
	private static final Set<ServerPlayer> gemArmorReadyChecks = Sets.newHashSet();
	private static final Set<ServerPlayer> hadFlightItem = Sets.newHashSet();
	private static final java.util.Map<ServerPlayer, Integer> projectileCooldowns = new java.util.HashMap<>();
	private static final java.util.Map<ServerPlayer, Integer> gemChestCooldowns = new java.util.HashMap<>();

	public static void resetProjectileCooldown(ServerPlayer player) {
		projectileCooldowns.put(player, ProjectEConfig.projectileCooldown);
	}

	public static int getProjectileCooldown(ServerPlayer player) {
		return projectileCooldowns.containsKey(player) ? projectileCooldowns.get(player) : -1;
	}

	public static void resetGemCooldown(ServerPlayer player) {
		gemChestCooldowns.put(player, ProjectEConfig.gemChestCooldown);
	}

	public static int getGemCooldown(ServerPlayer player) {
		return gemChestCooldowns.containsKey(player) ? gemChestCooldowns.get(player) : -1;
	}

	public static void setGemState(ServerPlayer player, boolean state)
	{
		if (state)
		{
			gemArmorReadyChecks.add(player);
		}
		else
		{
			gemArmorReadyChecks.remove(player);
		}
	}

	public static boolean getGemState(ServerPlayer player)
	{
		return gemArmorReadyChecks.contains(player);
	}

	// Checks if the server state of player capas mismatches with what ProjectE determines. If so, change it serverside and send a packet to client
	public static void update(ServerPlayer player)
	{
		if (projectileCooldowns.containsKey(player) && projectileCooldowns.get(player) > 0) {
			projectileCooldowns.put(player, projectileCooldowns.get(player) - 1);
		}

		if (gemChestCooldowns.containsKey(player) && gemChestCooldowns.get(player) > 0) {
			gemChestCooldowns.put(player, gemChestCooldowns.get(player) - 1);
		}

		if (!shouldPlayerFly(player) && hadFlightItem.contains(player))
		{
			if (player.capabilities.allowFlying)
			{
				PlayerHelper.updateClientServerFlight(player, false);
			}
			
			hadFlightItem.remove(player);
		}
		else if(shouldPlayerFly(player) && (!player.capabilities.allowFlying || !hadFlightItem.contains(player)))
		{
			// Re-assert flight whenever the player should have it (fixes the
			// capability reset on respawn after death).
			PlayerHelper.updateClientServerFlight(player, true);
			hadFlightItem.add(player);
		}

		if (!shouldPlayerStep(player))
		{
			if (player.stepHeight > 0.5F)
			{
				PlayerHelper.updateClientServerStepHeight(player, 0.5F);
			}
		}
		else
		{
			if (player.stepHeight < 1.0F)
			{
				PlayerHelper.updateClientServerStepHeight(player, 1.0F);
			}
		}
	}


	public static void onPlayerChangeDimension(ServerPlayer playerMP)
	{
		// Resend everything needed on clientside (all except fire resist)
		PlayerHelper.updateClientServerFlight(playerMP, playerMP.capabilities.allowFlying);
		PlayerHelper.updateClientServerStepHeight(playerMP, shouldPlayerStep(playerMP) ? 1.0F : 0.5F);
	}

	private static boolean shouldPlayerFly(ServerPlayer player)
	{
		if (!hasSwrg(player))
		{
			disableSwrgFlightOverride(player);
		}

		if (player.capabilities.isCreativeMode || swrgOverrides.contains(player))
		{
			return true;
		}

		for (ItemStack stack : player.inventory.armorInventory)
		{
			if (stack != null
					&& stack.getItem() instanceof IFlightProvider
					&& ((IFlightProvider) stack.getItem()).canProvideFlight(stack, player))
			{
				return true;
			}
		}

		for (int i = 0; i <= 8; i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);

			if (stack != null
					&& stack.getItem() instanceof IFlightProvider
					&& ((IFlightProvider) stack.getItem()).canProvideFlight(stack, player))
			{
				return true;
			}
		}

		IInventory baubles = PlayerHelper.getBaubles(player);
		if (baubles != null)
		{
			for (int i = 0; i < baubles.getSizeInventory(); i++)
			{
				ItemStack stack = baubles.getStackInSlot(i);
				if (stack != null
						&& stack.getItem() instanceof IFlightProvider
						&& ((IFlightProvider) stack.getItem()).canProvideFlight(stack, player))
				{
					return true;
				}
			}
		}

		return false;
	}
	
	public static boolean shouldPlayerResistFire(EntityPlayer player)
	{
		for (ItemStack stack : player.inventory.armorInventory)
		{
			if (stack != null
					&& stack.getItem() instanceof IFireProtector
					&& ((IFireProtector) stack.getItem()).canProtectAgainstFire(stack, player))
			{
				return true;
			}
		}

		for (int i = 0; i <= 8; i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);

			if (stack != null
					&& stack.getItem() instanceof IFireProtector
					&& ((IFireProtector) stack.getItem()).canProtectAgainstFire(stack, player))
			{
				return true;
			}
		}

		IInventory baubles = PlayerHelper.getBaubles(player);
		if (baubles != null)
		{
			for (int i = 0; i < baubles.getSizeInventory(); i++)
			{
				ItemStack stack = baubles.getStackInSlot(i);
				if (stack != null
						&& stack.getItem() instanceof IFireProtector
						&& ((IFireProtector) stack.getItem()).canProtectAgainstFire(stack, player))
				{
					return true;
				}
			}
		}

		return false;
	}
	
	private static boolean shouldPlayerStep(ServerPlayer player)
	{
		for (ItemStack stack : player.inventory.armorInventory)
		{
			if (stack != null
					&& stack.getItem() instanceof IStepAssister
					&& ((IStepAssister) stack.getItem()).canAssistStep(stack, player))
			{
				return true;
			}
		}

		for (int i = 0; i <= 8; i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);

			if (stack != null
					&& stack.getItem() instanceof IStepAssister
					&& ((IStepAssister) stack.getItem()).canAssistStep(stack, player))
			{
				return true;
			}
		}

		IInventory baubles = PlayerHelper.getBaubles(player);
		if (baubles != null)
		{
			for (int i = 0; i < baubles.getSizeInventory(); i++)
			{
				ItemStack stack = baubles.getStackInSlot(i);
				if (stack != null
						&& stack.getItem() instanceof IStepAssister
						&& ((IStepAssister) stack.getItem()).canAssistStep(stack, player))
				{
					return true;
				}
			}
		}

		return false;
	}

	private static boolean hasSwrg(ServerPlayer player)
	{
		for (int i = 0; i <= 8; i++)
		{
			if (player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i].getItem() == ObjHandler.swrg)
			{
				return true;
			}
		}

		IInventory baubles = PlayerHelper.getBaubles(player);
		if (baubles != null)
		{
			for (int i = 0; i < baubles.getSizeInventory(); i++)
			{
				if (baubles.getStackInSlot(i) != null && baubles.getStackInSlot(i).getItem() == ObjHandler.swrg)
				{
					return true;
				}
			}
		}
		return false;
	}

	public static void enableSwrgFlightOverride(ServerPlayer player)
	{
		swrgOverrides.add(player);
	}

	public static void disableSwrgFlightOverride(ServerPlayer player)
	{
		swrgOverrides.remove(player);
	}

	public static void clearLists()
	{
		swrgOverrides.clear();
		gemArmorReadyChecks.clear();
		hadFlightItem.clear();
		projectileCooldowns.clear();
	}

	public static void removePlayerFromLists(ServerPlayer player)
	{
		swrgOverrides.remove(player);
		gemArmorReadyChecks.remove(player);
		hadFlightItem.remove(player);
		projectileCooldowns.remove(player);
	}
}
