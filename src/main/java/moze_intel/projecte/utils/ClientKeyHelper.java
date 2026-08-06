package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableBiMap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Clientside key helper - because PEKeybind cannot touch client classes or it will crash dedicated servers
 */
@SideOnly(Side.CLIENT)
public class ClientKeyHelper
{
    public static ImmutableBiMap<KeyBinding, PEKeybind> mcToPe;
    public static ImmutableBiMap<PEKeybind, KeyBinding> peToMc;

    private static boolean registered;

    /**
     * Returns true only the first time; used to guard the GameSettings array
     * expansion in ProjectEKeybindingsMixin (same pattern as NewShop).
     */
    public static boolean markRegistered()
    {
        if (registered) return false;
        registered = true;
        return true;
    }

    /**
     * Creates the ProjectE KeyBinding objects and the PE<->MC maps, then
     * returns them so the GameSettings mixin can append them to the keybinding
     * array. Called by the mixin during GameSettings.initKeybindings.
     */
    public static KeyBinding[] createBindings()
    {
        ImmutableBiMap.Builder<KeyBinding, PEKeybind> builder = ImmutableBiMap.builder();
        List<KeyBinding> bindings = new ArrayList<>();
        for (PEKeybind k : PEKeybind.values())
        {
            KeyBinding mcK = new KeyBinding(k.keyName, k.defaultKeyCode);
            builder.put(mcK, k);
            bindings.add(mcK);
        }
        mcToPe = builder.build();
        peToMc = mcToPe.inverse();
        return bindings.toArray(new KeyBinding[0]);
    }

    /**
     * Safety net: normally the GameSettings mixin already created the
     * bindings before the client tick starts polling; this only creates them
     * if for some reason the mixin did not run.
     */
    public static void ensureBindings()
    {
        if (mcToPe == null)
        {
            createBindings();
        }
    }

    /**
     * Get the key name this PEKeybind is bound to.
     */
    public static String getKeyName(PEKeybind k)
    {
        int keyCode = peToMc.get(k).keyCode;
        if (keyCode > Keyboard.getKeyCount() || keyCode < 0)
        {
            return "INVALID KEY";
        }
        return Keyboard.getKeyName(keyCode);
    }

    public static String getKeyName(KeyBinding k)
    {
        int keyCode = k.keyCode;
        if (keyCode > Keyboard.getKeyCount() || keyCode < 0)
        {
            return "INVALID KEY";
        }
        return Keyboard.getKeyName(keyCode);
    }
}
