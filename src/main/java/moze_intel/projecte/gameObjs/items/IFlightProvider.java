package moze_intel.projecte.gameObjs.items;

import net.minecraft.ServerPlayer;
import net.minecraft.ItemStack;

/**
 * Internal interface for PlayerChecks.
 */
public interface IFlightProvider
{
    /**
     * @return If this stack currently should provide its bearer flight
     */
    boolean canProvideFlight(ItemStack stack, ServerPlayer player);
}
