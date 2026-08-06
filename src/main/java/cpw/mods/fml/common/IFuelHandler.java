package cpw.mods.fml.common;

import net.minecraft.ItemStack;

public interface IFuelHandler {
    int getBurnTime(ItemStack fuel);
}
