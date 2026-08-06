package net.minecraftforge.event.entity.player;

import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;

import java.util.List;

public class ItemTooltipEvent extends PlayerEvent {
    public final ItemStack itemStack;
    public final List<String> toolTip;
    public final boolean showAdvancedInfo;

    public ItemTooltipEvent(ItemStack itemStack, EntityPlayer player, List<String> toolTip, boolean showAdvancedInfo) {
        super(player);
        this.itemStack = itemStack;
        this.toolTip = toolTip;
        this.showAdvancedInfo = showAdvancedInfo;
    }
}
