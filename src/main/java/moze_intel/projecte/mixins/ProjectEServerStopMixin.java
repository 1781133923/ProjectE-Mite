package moze_intel.projecte.mixins;

import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The FML shim never posts FMLServerStopping/StoppedEvent, so PECore's
 * serverStopping()/serverQuit() cleanup (in-memory transmutation/bag props,
 * cached EMC, player checks) never ran when a world closed - stale
 * knowledge/EMC leaked into the next world you entered. Hook MinecraftServer
 * stopServer() at RETURN (player data is already on disk by then) and fire
 * both events so the cleanup actually happens.
 */
@Mixin(MinecraftServer.class)
public abstract class ProjectEServerStopMixin
{
	@Inject(method = "stopServer", at = @At("RETURN"))
	private void projecte$onServerStop(CallbackInfo ci)
	{
		moze_intel.projecte.PECore.instance.serverStopping(new FMLServerStoppingEvent());
		moze_intel.projecte.PECore.instance.serverQuit(new FMLServerStoppedEvent());
	}
}