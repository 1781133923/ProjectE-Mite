package moze_intel.projecte.mixins;

import net.minecraft.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE's EntityPlayer.clonePlayer() is a stripped-down copy that never fires
 * Forge's PlayerEvent.Clone, so ProjectE's PlayerEvents.cloneEvent (which
 * preserves transmutation knowledge/EMC and alchemy bag contents across
 * death) never runs. Re-publish the event here, matching vanilla Forge.
 */
@Mixin(EntityPlayer.class)
public abstract class PlayerCloneEventMixin {
    @Inject(method = "clonePlayer", at = @At("RETURN"))
    private void projecte$fireCloneEvent(EntityPlayer original, boolean wasDeath, CallbackInfo ci) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(original, self, wasDeath));
    }
}
