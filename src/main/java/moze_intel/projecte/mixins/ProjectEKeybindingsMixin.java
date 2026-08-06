package moze_intel.projecte.mixins;

import moze_intel.projecte.utils.ClientKeyHelper;
import net.minecraft.GameSettings;
import net.minecraft.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * Appends the ProjectE key bindings to MITE's GameSettings array during
 * initKeybindings - the same approach the NewShop mod uses for its keys. This
 * makes them appear in the Controls screen and persist in options.txt without
 * relying on RustedIronCore's keybinding hook.
 */
@Mixin(GameSettings.class)
public abstract class ProjectEKeybindingsMixin
{
	@Shadow
	public KeyBinding[] keyBindings;

	@Inject(method = "initKeybindings", at = @At("RETURN"))
	private void projecte$injectCustomKeys(CallbackInfo ci)
	{
		if (!ClientKeyHelper.markRegistered())
		{
			return;
		}

		KeyBinding[] custom = ClientKeyHelper.createBindings();
		KeyBinding[] expanded = Arrays.copyOf(keyBindings, keyBindings.length + custom.length);
		System.arraycopy(custom, 0, expanded, keyBindings.length, custom.length);
		keyBindings = expanded;
	}
}
