package moze_intel.projecte.mixins;

import moze_intel.projecte.ProjectE;
import net.minecraft.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs ProjectE's registry finalisation at the very start of
 * Minecraft.startGame - after every mod's onInitialize has completed, but
 * before MITE's item atlas pass that assigns each item its icon. Previously
 * this ran at the end of startGame (manylib init), so items created by the
 * late ItemRegistryEvent (UncannyBaubles' baubles) missed the atlas pass
 * entirely, kept a null itemIcon and rendered as the missing-texture
 * purple/black blocks.
 */
@Mixin(Minecraft.class)
public abstract class ProjectEStartGameMixin
{
	@Inject(method = "startGame", at = @At("HEAD"))
	private void projecte$finalizeRegistryBeforeAtlas(CallbackInfo ci)
	{
		ProjectE.finalizeRecipeRegistration();
	}
}
