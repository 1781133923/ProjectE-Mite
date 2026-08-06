package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.SearchUpdatePKT;
import net.minecraft.Container;
import net.minecraft.GuiContainer;
import net.minecraft.ItemStack;
import net.minecraft.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE's {@link Container#slotClick} is final, so the original ProjectE
 * override that tells the server which item was displayed in a transmutation
 * output slot never runs. This restores that behaviour by notifying the server
 * right before the vanilla click is processed.
 */
@Mixin(GuiContainer.class)
public abstract class TransmutationGuiClickMixin {
    @Shadow
    public Container inventorySlots;

    @Inject(method = "handleMouseClick", at = @At("HEAD"))
    private void projecte$notifyTransmutationOutputClick(Slot slot, int slotIndex, int button, int modifier, CallbackInfo ci) {
        if (this.inventorySlots instanceof TransmutationContainer && slotIndex >= 10 && slotIndex <= 25) {
            ItemStack stack = slot != null ? slot.getStack() : null;
            if (stack != null) {
                PacketHandler.sendToServer(new SearchUpdatePKT(slotIndex, stack));
            }
        }
    }
}
