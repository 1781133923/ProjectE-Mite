package net.minecraftforge.event.entity.living;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.EntityLivingBase;

public class LivingEvent extends Event {
    public final EntityLivingBase entityLiving;

    public LivingEvent(EntityLivingBase entity) {
        this.entityLiving = entity;
    }
}
