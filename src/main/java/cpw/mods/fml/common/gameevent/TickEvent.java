package cpw.mods.fml.common.gameevent;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.EntityPlayer;

public class TickEvent extends Event {
    public final Phase phase;
    public final Side side = Side.SERVER;

    public TickEvent(Phase phase) {
        this.phase = phase;
    }

    public enum Phase {
        START,
        END
    }

    public static class ServerTickEvent extends TickEvent {
        public ServerTickEvent(Phase phase) {
            super(phase);
        }
    }

    public static class ClientTickEvent extends TickEvent {
        public ClientTickEvent(Phase phase) {
            super(phase);
        }
    }

    public static class RenderTickEvent extends TickEvent {
        public final float renderTickTime;

        public RenderTickEvent(Phase phase, float renderTickTime) {
            super(phase);
            this.renderTickTime = renderTickTime;
        }
    }

    public static class PlayerTickEvent extends TickEvent {
        public final EntityPlayer player;

        public PlayerTickEvent(Phase phase, EntityPlayer player) {
            super(phase);
            this.player = player;
        }
    }
}
