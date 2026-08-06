package codechicken.nei.api;

import net.minecraft.ItemStack;

public interface ItemFilter {
    boolean matches(ItemStack stack);
}
