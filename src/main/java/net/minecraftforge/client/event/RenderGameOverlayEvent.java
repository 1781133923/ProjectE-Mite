package net.minecraftforge.client.event;

import cpw.mods.fml.common.eventhandler.Event;

public class RenderGameOverlayEvent extends Event {
    public final ElementType type;
    public final float partialTicks;
    public final ScaledResolution resolution;

    public RenderGameOverlayEvent(ElementType type, ScaledResolution resolution, float partialTicks) {
        this.type = type;
        this.resolution = resolution;
        this.partialTicks = partialTicks;
    }

    public static class Pre extends RenderGameOverlayEvent {
        public Pre(ElementType type, ScaledResolution resolution, float partialTicks) {
            super(type, resolution, partialTicks);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    public enum ElementType {
        ALL,
        HELMET,
        PORTAL,
        CROSSHAIRS,
        BOSSHEALTH,
        ARMOR,
        HEALTH,
        FOOD,
        AIR,
        HOTBAR,
        EXPERIENCE,
        TEXT,
        HEALTHMOUNT,
        CHAT,
        PLAYER_LIST,
        DEBUG,
        POTION_ICONS,
        SUBTITLES,
        FPS_GRAPH,
        VIGNETTE
    }

    public static class ScaledResolution {
        public final int scaledWidth;
        public final int scaledHeight;

        public ScaledResolution(int scaledWidth, int scaledHeight) {
            this.scaledWidth = scaledWidth;
            this.scaledHeight = scaledHeight;
        }

        public int getScaledWidth() {
            return this.scaledWidth;
        }

        public int getScaledHeight() {
            return this.scaledHeight;
        }
    }
}
