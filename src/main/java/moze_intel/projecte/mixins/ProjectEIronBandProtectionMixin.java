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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * The iron band grants +0.5 armour while carried. Only one band can be
 * effective regardless of how many are carried. It is detected in the main
 * inventory/armor, in Baubles ring slots, and inside simplebackpack
 * backpacks (reflection, so the mod may be absent).
 */
@Mixin(EntityLivingBase.class)
public abstract class ProjectEIronBandProtectionMixin {
    private static Constructor<?> backpackCtor;
    private static Method backpackSizeMethod;
    private static Method backpackSlotMethod;

    @Inject(method = "getProtectionFromArmor", at = @At("RETURN"), cancellable = true)
    private void projecte$ironBandArmourBonus(DamageSource source, boolean flag, CallbackInfoReturnable<Float> cir) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        if (self instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) self;
            if (player.ticksExisted % 40 == 0) {
                System.out.println("[ProjectE] iron-band: fired, has=" + hasIronBand(player)
                        + " prot=" + cir.getReturnValueF()
                        + " baubles=" + (moze_intel.projecte.utils.PlayerHelper.getBaubles(player) != null)
                        + " invIron=" + countIronBand(player));
            }
            if (hasIronBand(player)) {
                cir.setReturnValue(cir.getReturnValueF() + 0.5F);
            }
        }
    }

    private static int countIronBand(EntityPlayer player) {
        int count = 0;
        net.minecraft.Item ironBand = moze_intel.projecte.gameObjs.ObjHandler.ironBand;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack s = player.inventory.getStackInSlot(i);
            if (s != null && s.getItem() == ironBand) {
                count++;
            }
        }
        return count;
    }

    private static boolean hasIronBand(EntityPlayer player) {
        net.minecraft.Item ironBand = moze_intel.projecte.gameObjs.ObjHandler.ironBand;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack s = player.inventory.getStackInSlot(i);
            if (s != null) {
                if (s.getItem() == ironBand) {
                    return true;
                }
                if (hasIronBandInBackpack(s, ironBand)) {
                    return true;
                }
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

    /**
     * Checks a simplebackpack (com.moddedmite.sbp) backpack stack's contents
     * via reflection. Returns false when the mod is absent or the call fails.
     */
    private static boolean hasIronBandInBackpack(ItemStack backpackStack, net.minecraft.Item ironBand) {
        try {
            net.minecraft.Item item = backpackStack.getItem();
            if (item == null || !"com.moddedmite.sbp.ItemBackpack".equals(item.getClass().getName())) {
                return false;
            }
            if (backpackCtor == null) {
                Class<?> invClass = Class.forName("com.moddedmite.sbp.InventoryBackpack");
                Class<?> itemClass = Class.forName("com.moddedmite.sbp.ItemBackpack");
                backpackCtor = invClass.getConstructor(net.minecraft.ItemStack.class, itemClass);
                backpackSizeMethod = invClass.getMethod("getSizeInventory");
                backpackSlotMethod = invClass.getMethod("getStackInSlot", int.class);
            }
            Object inv = backpackCtor.newInstance(backpackStack, item);
            int size = ((Number) backpackSizeMethod.invoke(inv)).intValue();
            for (int i = 0; i < size; i++) {
                ItemStack s = (ItemStack) backpackSlotMethod.invoke(inv, i);
                if (s != null && s.getItem() == ironBand) {
                    return true;
                }
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }
}