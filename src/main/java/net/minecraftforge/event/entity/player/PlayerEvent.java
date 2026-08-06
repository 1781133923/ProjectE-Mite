package net.minecraftforge.event.entity.player;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.EntityPlayer;

public class PlayerEvent extends Event {
    public final EntityPlayer entityPlayer;

    public PlayerEvent(EntityPlayer player) {
        this.entityPlayer = player;
    }

    public static class Clone extends PlayerEvent {
        public final EntityPlayer original;
        public final boolean wasDeath;

        public Clone(EntityPlayer original, EntityPlayer player, boolean wasDeath) {
            super(player);
            this.original = original;
            this.wasDeath = wasDeath;
        }
    }
}
