package thaumcraft.api;

import net.minecraft.EntityLivingBase;
import net.minecraft.ItemStack;

public interface IGoggles {
    boolean showIngamePopups(ItemStack itemstack, EntityLivingBase player);
}
