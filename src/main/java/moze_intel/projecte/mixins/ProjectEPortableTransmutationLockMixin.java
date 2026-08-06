package moze_intel.projecte.mixins;

import net.minecraft.Container;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE's Container.slotClick is final and never calls the Forge-era
 * handleSlotClick hook that TransmutationContainer used, so the portable
 * transmutation tablet was never locked in place while its GUI was open.
 * Re-apply the lock here: while the portable tablet GUI is open and the
 * player is holding the tablet, only clicks on the slot that contains the
 * held tablet itself are rejected, so the tablet cannot be picked up,
 * shift-moved, hotbar-swapped or otherwise moved out of the hand. Every
 * other slot in the GUI (transmutation input/output slots and the rest of
 * the backpack) stays fully usable.
 */
@Mixin(Container.class)
public abstract class ProjectEPortableTransmutationLockMixin {
    @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true)
    private void projecte$lockHeldPortableTablet(int slot, int button, int flag, boolean holding_shift,
                                                 EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        net.minecraft.Container self = (net.minecraft.Container) (Object) this;
        if (!(self instanceof moze_intel.projecte.gameObjs.container.TransmutationContainer)) {
            return;
        }
        if (player == null || player.getHeldItemStack() == null) {
            return;
        }
        if (player.getHeldItemStack().getItem() != moze_intel.projecte.gameObjs.ObjHandler.transmutationTablet) {
            return;
        }
        // Only the slot holding the exact held tablet stack is locked; all
        // other clicks (moving other backpack items, pulling outputs,
        // consuming for EMC, ...) go through normally.
        if (slot >= 0 && slot < self.inventorySlots.size()) {
            net.minecraft.Slot target = self.getSlot(slot);
            if (target != null && target.getStack() != null
                    && target.getStack() == player.getHeldItemStack()
                    && target.getStack().getItem() == moze_intel.projecte.gameObjs.ObjHandler.transmutationTablet) {
                cir.setReturnValue(null);
            }
        }
    }
}
