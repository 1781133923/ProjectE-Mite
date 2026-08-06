package moze_intel.projecte.mixins;

import moze_intel.projecte.ProjectE;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * On dedicated servers the item/block registry events used to be deferred to
 * the first server tick (ProjectE.onServerStarting), which runs AFTER the
 * world - and the structure classes with their loot listeners - have already
 * loaded. Mods that construct their items inside the ItemRegistryEvent (e.g.
 * UncannyBaubles) were therefore still null when the loot tables registered,
 * crashing the server (new ItemStack(null)). Fire the registry finalisation at
 * MinecraftServer construction instead, right after every mod's onInitialize
 * has run and before any world loads. On clients / integrated servers this is
 * a guarded no-op (already done at startGame HEAD).
 */
@Mixin(MinecraftServer.class)
public abstract class ProjectEServerStartMixin
{
	@Inject(method = "<init>", at = @At("RETURN"))
	private void projecte$finalizeRegistryBeforeWorldLoad(CallbackInfo ci)
	{
		ProjectE.finalizeRecipeRegistration();
	}
}
