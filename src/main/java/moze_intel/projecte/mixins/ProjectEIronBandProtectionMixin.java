package moze_intel.projecte.mixins;

import net.minecraft.DamageSource;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The iron band grants +0.5 armour while carried. Only one band can be
 * effective regardless of how many are carried, and the pedestal (which has
 * no iron-band function) is unaffected.
 */
@Mixin(EntityLivingBase.class)
public abstract class ProjectEIronBandProtectionMixin {
    @Inject(method = "getProtectionFromArmor", at = @At("RETURN"), cancellable = true)
    private void projecte$ironBandArmourBonus(DamageSource source, boolean flag, CallbackInfoReturnable<Float> cir) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        if (self instanceof EntityPlayer && hasIronBand((EntityPlayer) self)) {
            cir.setReturnValue(cir.getReturnValueF() + 0.5F);
        }
    }

    private static boolean hasIronBand(EntityPlayer player) {
        net.minecraft.Item ironBand = moze_intel.projecte.gameObjs.ObjHandler.ironBand;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack s = player.inventory.getStackInSlot(i);
            if (s != null && s.getItem() == ironBand) {
                return true;
            }
        }
        for (ItemStack s : player.inventory.armorInventory) {
            if (s != null && s.getItem() == ironBand) {
                return true;
            }
        }
        IInventory baubles = moze_intel.projecte.utils.PlayerHelper.getBaubles(player);
        if (baubles != null) {
            for (int i = 0; i < baubles.getSizeInventory(); i++) {
                ItemStack s = baubles.getStackInSlot(i);
                if (s != null && s.getItem() == ironBand) {
                    return true;
                }
            }
        }
        return false;
    }
}
