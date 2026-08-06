package net.minecraftforge.event.entity.player;

import net.minecraft.EntityItem;
import net.minecraft.EntityPlayer;

public class EntityItemPickupEvent extends PlayerEvent {
    public final EntityItem item;

    public EntityItemPickupEvent(EntityPlayer player, EntityItem item) {
        super(player);
        this.item = item;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
