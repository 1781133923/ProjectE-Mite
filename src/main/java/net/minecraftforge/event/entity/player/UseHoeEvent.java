package net.minecraftforge.event.entity.player;

import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.World;

public class UseHoeEvent extends PlayerEvent {
    public final World world;
    public final int x;
    public final int y;
    public final int z;
    public final ItemStack current;

    public UseHoeEvent(EntityPlayer player, ItemStack current, World world, int x, int y, int z) {
        super(player);
        this.current = current;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
