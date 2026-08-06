package net.minecraftforge.event.world;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.Explosion;
import net.minecraft.World;

public class ExplosionEvent extends Event {
    public final World world;
    public final Explosion explosion;

    public ExplosionEvent(World world, Explosion explosion) {
        this.world = world;
        this.explosion = explosion;
    }

    public static class Start extends ExplosionEvent {
        public Start(World world, Explosion explosion) {
            super(world, explosion);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    public static class Detonate extends ExplosionEvent {
        public Detonate(World world, Explosion explosion) {
            super(world, explosion);
        }
    }
}
