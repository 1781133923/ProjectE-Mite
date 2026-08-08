package moze_intel.projecte.mixins;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MITE's MinecraftServer.tick calls ResourceLocation.verifyResourceLocations()
 * every 20 ticks. That method iterates a plain static ArrayList which the
 * client thread keeps appending to (every ResourceLocation created with
 * verification pending), so under the resulting race a null entry can show up
 * mid-iteration and crash the integrated server with a NPE. Replace the call
 * with a null-safe version that still verifies and clears the list.
 */
@Mixin(MinecraftServer.class)
public abstract class ProjectEResourceVerifyMixin
{
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/ResourceLocation;verifyResourceLocations()V"))
    private void projecte$verifyResourceLocationsNullSafe()
    {
        try
        {
            java.lang.reflect.Field field = net.minecraft.ResourceLocation.class.getDeclaredField("resources_to_verify");
            field.setAccessible(true);
            java.util.List list = (java.util.List) field.get(null);
            if (list == null)
            {
                return;
            }
            int size = list.size();
            for (int i = 0; i < size; i++)
            {
                Object o = list.get(i);
                if (o instanceof net.minecraft.ResourceLocation)
                {
                    ((net.minecraft.ResourceLocation) o).verifyExistence();
                }
            }
            list.clear();
        }
        catch (Throwable t)
        {
            // Never let resource verification crash the server.
        }
    }
}