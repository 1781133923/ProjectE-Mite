package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.container.PhilosStoneContainer;
import net.minecraft.ClientPlayer;
import net.minecraft.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Philosopher's Stone crafting table counts as an adamantium-tier
 * workbench: the crafting-speed modifier is the same as an adamantium bench
 * (0.7). (All recipes are already allowed there, since MITE only enforces the
 * bench requirement for actual ContainerWorkbench blocks.)
 */
@Mixin(ClientPlayer.class)
public abstract class ProjectEPhiloBenchMixin
{
	@Inject(method = "getBenchAndToolsModifier(Lnet/minecraft/Container;)F",
			at = @At("HEAD"), cancellable = true)
	private void projecte$philoBenchSpeed(Container container, CallbackInfoReturnable<Float> cir)
	{
		if (container instanceof PhilosStoneContainer)
		{
			cir.setReturnValue(0.7F);
		}
	}
}
