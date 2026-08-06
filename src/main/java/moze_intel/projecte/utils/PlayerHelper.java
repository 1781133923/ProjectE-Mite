package moze_intel.projecte.utils;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.Loader;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.SetFlyPKT;
import moze_intel.projecte.network.packets.StepHeightPKT;
import moze_intel.projecte.network.packets.SwingItemPKT;
import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.IInventory;
import net.minecraft.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.MovingObjectPosition;
import net.minecraft.Vec3;
import net.minecraft.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Helper class for player-related methods.
 * Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class PlayerHelper
{
	/**
	 * Tries placing a block and fires an event for it.
	 * @return Whether the block was successfully placed
	 */
	public static boolean checkedPlaceBlock(ServerPlayer player, int x, int y, int z, Block toPlace, int toPlaceMeta)
	{
		if (!hasEditPermission(player, x, y, z))
		{
			return false;
		}
		World world = player.worldObj;
		BlockSnapshot before = new BlockSnapshot(world, x, y, z, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
		world.setBlock(x, y, z, toPlace.blockID);
		world.setBlockMetadataWithNotify(x, y, z, toPlaceMeta, 3);
		BlockEvent.PlaceEvent evt = new BlockEvent.PlaceEvent(before.getBlock(), Blocks.air, player); // Todo verify can use air here
		MinecraftForge.EVENT_BUS.post(evt);
		if (evt.isCanceled())
		{
			
			
			
			//PELogger.logInfo("Checked place block got canceled, restoring snapshot.");
			return false;
		}
		//PELogger.logInfo("Checked place block passed!");
		return true;
	}

	public static boolean checkedReplaceBlock(ServerPlayer player, int x, int y, int z, Block toPlace, int toPlaceMeta)
	{
		return hasBreakPermission(player, x, y, z) && checkedPlaceBlock(player, x, y, z, toPlace, toPlaceMeta);
	}

	public static ItemStack findFirstItem(EntityPlayer player, ItemPE consumeFrom)
	{
		for (ItemStack s : player.inventory.mainInventory)
		{
			if (s != null && s.getItem() == consumeFrom)
			{
				return s;
			}
		}
		return null;
	}

	public static IInventory getBaubles(EntityPlayer player)
	{
		if (!Loader.isModLoaded("Baubles"))
		{
			return null;
		} else
		{
			return BaublesApi.getBaubles(player);
		}
	}

	public static Vec3 getBlockLookingAt(EntityPlayer player, double maxDistance)
	{
		Pair<Vec3, Vec3> vecs = getLookVec(player, maxDistance);
		MovingObjectPosition mop = moze_intel.projecte.compat.PECompatHelper.rayTrace(player.worldObj, vecs.getLeft(), vecs.getRight());
		if (mop != null && mop.getEntityHit() == null)
		{
			ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
			return Vec3.createVectorHelper(mop.blockX + dir.offsetX * 1.1, mop.blockY + dir.offsetY * 1.1, mop.blockZ + dir.offsetZ * 1.1);
		}
		return null;
	}

	/**
	 * Returns a vec representing where the player is looking, capped at maxDistance away.
	 */
	public static Pair<Vec3, Vec3> getLookVec(EntityPlayer player, double maxDistance)
	{
		// Thank you ForgeEssentials
		Vec3 look = player.getLook(1.0F);
		Vec3 playerPos = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3 src = playerPos.addVector(0, player.getEyeHeight(), 0);
		Vec3 dest = src.addVector(look.xCoord * maxDistance, look.yCoord * maxDistance, look.zCoord * maxDistance);
		return ImmutablePair.of(src, dest);
	}

	public static boolean hasBreakPermission(ServerPlayer player, int x, int y, int z)
	{
		return hasEditPermission(player, x, y, z)
				&& ForgeHooks.onBlockBreakEvent(player.worldObj, 0, player, x, y, z) != -1;
	}

	public static boolean hasEditPermission(ServerPlayer player, int x, int y, int z)
	{
		return player.canPlayerEdit(x, y, z, null)
				&& !MinecraftServer.getServer().isBlockProtected(player.worldObj, x, y, z, player);
	}


	public static void setPlayerFireImmunity(EntityPlayer player, boolean value)
	{
		// MITE has no isImmuneToFire field; fire immunity is driven by
		// EntityLivingBase.isHarmedByFire(), which ProjectE handles through
		// ProjectEFireImmunityMixin. Nothing to toggle here.
	}

	public static void setPlayerWalkSpeed(EntityPlayer player, float value)
	{
		// MITE exposes a public setter on PlayerCapabilities, so no reflection
		// is needed (the 1.7.10 obfuscated field names do not exist at runtime).
		player.capabilities.setPlayerWalkSpeed(value);
	}

	public static void swingItem(EntityPlayer player)
	{
		if (player instanceof ServerPlayer)
		{
			PacketHandler.sendTo(new SwingItemPKT(), ((ServerPlayer) player));
		}
	}

	public static void updateClientServerFlight(ServerPlayer player, boolean state)
	{
		PacketHandler.sendTo(new SetFlyPKT(state), player);
		player.capabilities.allowFlying = state;

		if (!state)
		{
			player.capabilities.isFlying = false;
		}
	}

	public static void updateClientServerStepHeight(ServerPlayer player, float value)
	{
		player.stepHeight = value;
		PacketHandler.sendTo(new StepHeightPKT(value), player);
	}
}
