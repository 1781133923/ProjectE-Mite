package cpw.mods.fml.common.network;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class NetworkRegistry {
    public static final NetworkRegistry INSTANCE = new NetworkRegistry();

    private IGuiHandler guiHandler;

    public SimpleNetworkWrapper newSimpleChannel(String channelName) {
        return new SimpleNetworkWrapper(channelName);
    }

    public void registerGuiHandler(Object mod, IGuiHandler handler) {
        this.guiHandler = handler;
    }

    public IGuiHandler getGuiHandler() {
        return this.guiHandler;
    }

    public static class TargetPoint {
        private final int dimension;
        private final double x;
        private final double y;
        private final double z;
        private final double range;

        public TargetPoint(int dimension, double x, double y, double z, double range) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.range = range;
        }

        public int getDimension() {
            return this.dimension;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }

        public double getRange() {
            return this.range;
        }
    }
}
