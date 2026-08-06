package net.minecraftforge.client.event;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.EntityPlayer;

public class FOVUpdateEvent extends Event {
    public final EntityPlayer entity;
    public final float fov;
    public float newfov;

    public FOVUpdateEvent(EntityPlayer entity, float fov) {
        this.entity = entity;
        this.fov = fov;
        this.newfov = fov;
    }
}
