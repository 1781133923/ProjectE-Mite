package moze_intel.projecte.gameObjs.items;

import net.minecraft.ServerPlayer;
import net.minecraft.ItemStack;

/**
 * Internal interface for PlayerChecks.
 */
public interface IStepAssister
{
    /**
     * @return If this stack currently should enhance the bearer's step height
     */
    boolean canAssistStep(ItemStack stack, ServerPlayer player);
}
