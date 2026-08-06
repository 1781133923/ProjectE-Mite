package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.container.CondenserContainer;
import moze_intel.projecte.gameObjs.container.CondenserMK2Container;
import net.minecraft.Container;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE's Container.slotClick is final, so ProjectE's condenser lock-slot
 * override never runs. Restore the original behaviour: clicking the lock slot
 * (slot 0) with an item in it clears the lock.
 */
@Mixin(Container.class)
public abstract class CondenserLockClickMixin {
    @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true)
    private void projecte$condenserLockClick(int slot, int button, int flag, boolean holdingShift,
                                             EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        Object self = this;
        if (self instanceof CondenserContainer) {
            CondenserContainer container = (CondenserContainer) self;
            if (slot == 0 && container.tile.getStackInSlot(0) != null) {
                if (!player.worldObj.isRemote) {
                    container.tile.setInventorySlotContents(0, null);
                    container.tile.checkLockAndUpdate();
                    container.detectAndSendChanges();
                }
                cir.setReturnValue(null);
            }
        } else if (self instanceof CondenserMK2Container) {
            CondenserMK2Container container = (CondenserMK2Container) self;
            if (slot == 0 && container.tile.getStackInSlot(0) != null) {
                if (!player.worldObj.isRemote) {
                    container.tile.setInventorySlotContents(0, null);
                    container.tile.checkLockAndUpdate();
                    container.detectAndSendChanges();
                }
                cir.setReturnValue(null);
            }
        }
    }
}
