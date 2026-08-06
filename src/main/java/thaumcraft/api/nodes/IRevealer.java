package thaumcraft.api.nodes;

import net.minecraft.EntityLivingBase;
import net.minecraft.ItemStack;

public interface IRevealer {
    boolean showNodes(ItemStack itemstack, EntityLivingBase player);
}
