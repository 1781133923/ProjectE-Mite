package net.minecraftforge.event.entity;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.Entity;

public class EntityEvent extends Event {
    public final Entity entity;

    public EntityEvent(Entity entity) {
        this.entity = entity;
    }

    public static class EntityConstructing extends EntityEvent {
        public EntityConstructing(Entity entity) {
            super(entity);
        }
    }
}
