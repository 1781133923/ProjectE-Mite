package net.minecraftforge.client.event;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.EntityPlayer;
import net.minecraft.RenderPlayer;

public class RenderPlayerEvent extends Event {
    public final EntityPlayer entityPlayer;
    public final RenderPlayer renderer;
    public final float partialRenderTick;

    public RenderPlayerEvent(EntityPlayer player, RenderPlayer renderer, float partialTick) {
        this.entityPlayer = player;
        this.renderer = renderer;
        this.partialRenderTick = partialTick;
    }

    public static class Specials extends RenderPlayerEvent {
        public Specials(EntityPlayer player, RenderPlayer renderer, float partialTick) {
            super(player, renderer, partialTick);
        }

        public static class Pre extends Specials {
            public Pre(EntityPlayer player, RenderPlayer renderer, float partialTick) {
                super(player, renderer, partialTick);
            }

            @Override
            public boolean isCancelable() {
                return true;
            }
        }

        public static class Post extends Specials {
            public Post(EntityPlayer player, RenderPlayer renderer, float partialTick) {
                super(player, renderer, partialTick);
            }
        }
    }
}
