package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.entity.PEProjectile;
import net.minecraft.Entity;
import net.minecraft.EntityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE's EntityTracker only sends entities to clients if the class appears in
 * its vanilla instanceof chain (arrows, snowballs, ...). ProjectE's throwable
 * orbs/projectiles are not in that chain, so clients never received their
 * spawn packets and the orbs were completely invisible. Track them exactly
 * like the vanilla projectiles (range 256, updates every 10 ticks, with
 * velocity updates so the flight is smooth).
 */
@Mixin(EntityTracker.class)
public abstract class ProjectEEntityTrackerMixin
{
	@Inject(method = "addEntityToTracker(Lnet/minecraft/Entity;)V", at = @At("HEAD"), cancellable = true)
	private void projecte$trackPEProjectiles(Entity entity, CallbackInfo ci)
	{
		if (entity instanceof PEProjectile)
		{
			((EntityTracker) (Object) this).addEntityToTracker(entity, 256, 10, true);
			ci.cancel();
		}
	}
}
