package net.minecraftforge.client.event;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.MovingObjectPosition;

public class DrawBlockHighlightEvent extends Event {
    public final EntityPlayer player;
    public final MovingObjectPosition target;
    public final int subID;
    public final ItemStack currentItem;
    public final float partialTicks;

    public DrawBlockHighlightEvent(EntityPlayer player, MovingObjectPosition target, int subID, ItemStack currentItem, float partialTicks) {
        this.player = player;
        this.target = target;
        this.subID = subID;
        this.currentItem = currentItem;
        this.partialTicks = partialTicks;
    }
}
