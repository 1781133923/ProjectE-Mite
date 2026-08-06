package moze_intel.projecte.mixins;

import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When a mod that provided items is removed, stacks saved with that mod's
 * item ids still exist in player/world NBT. Item.getItem(id) returns null for
 * them, and vanilla ItemStack.readFromNBT then NPEs while formatting the
 * stack (Item.itemsList[itemID] is null). Reject unknown item ids up front so
 * the save loads normally and the lost stacks are simply dropped as empty
 * slots instead of crashing the game.
 */
@Mixin(ItemStack.class)
public abstract class ProjectEItemStackLoadMixin {
    @Inject(method = "loadItemStackFromNBT", at = @At("HEAD"), cancellable = true)
    private static void projecte$rejectUnknownItemIds(NBTTagCompound compound, CallbackInfoReturnable<ItemStack> cir) {
        if (compound == null || !compound.hasKey("id")) {
            return;
        }
        int id = compound.getShort("id");
        if (id > 0 && Item.getItem(id) == null) {
            cir.setReturnValue(null);
        }
    }
}
