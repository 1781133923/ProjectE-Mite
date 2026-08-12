package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.armor.GemArmorBase;
import net.minecraft.EntityWitch;
import net.minecraft.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Full gem armour set bonus: MITE witches cannot curse the wearer. The only
 * curse entry point is EntityWitch.cursePlayer -> WorldServer.addCurse, so
 * cancelling at the head of cursePlayer is complete.
 */
@Mixin(EntityWitch.class)
public abstract class ProjectEWitchCurseImmunityMixin
{
	@Inject(method = "cursePlayer", at = @At("HEAD"), cancellable = true)
	private void projecte$gemSetCurseImmunity(ServerPlayer player, CallbackInfo ci)
	{
		if (GemArmorBase.hasFullSet(player))
		{
			ci.cancel();
		}
	}
}