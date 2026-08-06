package moze_intel.projecte.mixins;

import moze_intel.projecte.compat.ExtendedProperties;
import moze_intel.projecte.playerData.AlchBagProps;
import moze_intel.projecte.playerData.TransmutationProps;
import net.minecraft.EntityPlayer;
import net.minecraft.NBTTagCompound;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Persists ProjectE player data (transmutation knowledge/EMC and alchemy bag
 * contents) into the player NBT, since the port uses an in-memory stand-in for
 * Forge's IExtendedEntityProperties attachment.
 */
@Mixin(EntityPlayer.class)
public abstract class PlayerPropsMixin {
    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    private void projecte$writeProps(NBTTagCompound compound, CallbackInfo ci) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        IExtendedEntityProperties transmute = ExtendedProperties.get(self, TransmutationProps.PROP_NAME);
        if (transmute != null) {
            transmute.saveNBTData(compound);
        }
        IExtendedEntityProperties bag = ExtendedProperties.get(self, AlchBagProps.PROP_NAME);
        if (bag != null) {
            bag.saveNBTData(compound);
        }
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    private void projecte$readProps(NBTTagCompound compound, CallbackInfo ci) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        TransmutationProps.getDataFor(self).loadNBTData(compound);
        AlchBagProps.getDataFor(self).loadNBTData(compound);
    }
}
