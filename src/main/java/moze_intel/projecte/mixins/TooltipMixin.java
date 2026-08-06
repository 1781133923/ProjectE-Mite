package moze_intel.projecte.mixins;

import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.Slot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * MITE does not fire Forge's ItemTooltipEvent, so ProjectE's EMC/OD tooltip
 * handler never ran. Fire it when the tooltip list is built, the same place
 * Forge 1.7.10 fires it from (ItemStack.getTooltip).
 */
@Mixin(ItemStack.class)
public abstract class TooltipMixin {
    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void projecte$fireTooltipEvent(EntityPlayer player, boolean showAdvancedInfo, Slot slot,
                                           CallbackInfoReturnable<List<String>> cir) {
        List<String> toolTip = cir.getReturnValue();
        if (toolTip == null) {
            return;
        }
        MinecraftForge.EVENT_BUS.post(new ItemTooltipEvent((ItemStack) (Object) this, player, toolTip, showAdvancedInfo));
    }
}
