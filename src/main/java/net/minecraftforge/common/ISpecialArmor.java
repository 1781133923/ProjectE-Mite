package net.minecraftforge.common;

import net.minecraft.DamageSource;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityLivingBase;
import net.minecraft.ItemStack;

public interface ISpecialArmor {
    ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot);

    int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot);

    default void armorBreak(EntityLivingBase entity, ItemStack armor) {
    }

    class ArmorProperties {
        public int priority;
        public double absorbRatio;
        public int absorbMax;

        public ArmorProperties(int priority, double absorbRatio, int absorbMax) {
            this.priority = priority;
            this.absorbRatio = absorbRatio;
            this.absorbMax = absorbMax;
        }
    }
}
