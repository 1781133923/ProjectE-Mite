package cpw.mods.fml.common.gameevent;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.server.MinecraftServer;

public class PlayerEvent extends Event {
    public final EntityPlayer player;

    public PlayerEvent(EntityPlayer player) {
        this.player = player;
    }

    public static class PlayerLoggedInEvent extends PlayerEvent {
        public PlayerLoggedInEvent(EntityPlayer player) {
            super(player);
        }
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(EntityPlayer player) {
            super(player);
        }
    }

    public static class PlayerChangedDimensionEvent extends PlayerEvent {
        public final int fromDim;
        public final int toDim;

        public PlayerChangedDimensionEvent(EntityPlayer player, int fromDim, int toDim) {
            super(player);
            this.fromDim = fromDim;
            this.toDim = toDim;
        }
    }

    public static class ItemPickupEvent extends PlayerEvent {
        public final ItemStack stack;

        public ItemPickupEvent(EntityPlayer player, ItemStack stack) {
            super(player);
            this.stack = stack;
        }
    }

    public static class ItemCraftedEvent extends PlayerEvent {
        public final ItemStack crafting;

        public ItemCraftedEvent(EntityPlayer player, ItemStack crafting) {
            super(player);
            this.crafting = crafting;
        }
    }

    public static class ItemSmeltedEvent extends PlayerEvent {
        public final ItemStack smelting;

        public ItemSmeltedEvent(EntityPlayer player, ItemStack smelting) {
            super(player);
            this.smelting = smelting;
        }
    }

    public static class PlayerRespawnEvent extends PlayerEvent {
        public PlayerRespawnEvent(EntityPlayer player) {
            super(player);
        }
    }

    public static class PlayerSleepInBedEvent extends PlayerEvent {
        public PlayerSleepInBedEvent(EntityPlayer player) {
            super(player);
        }
    }

    public static class PlayerWakeUpEvent extends PlayerEvent {
        public PlayerWakeUpEvent(EntityPlayer player) {
            super(player);
        }
    }
}
