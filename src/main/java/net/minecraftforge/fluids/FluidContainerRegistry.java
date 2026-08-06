package net.minecraftforge.fluids;

public class FluidContainerRegistry {
    public static final int BUCKET_VOLUME = 1000;

    private static final java.util.List<FluidContainerData> DATA = new java.util.ArrayList<>();

    public static void registerFluidContainer(FluidContainerData data) {
        DATA.add(data);
    }

    public static FluidContainerData[] getRegisteredFluidContainerData() {
        return DATA.toArray(new FluidContainerData[0]);
    }

    public static class FluidContainerData {
        public final FluidStack fluid;
        public final net.minecraft.ItemStack filledContainer;
        public final net.minecraft.ItemStack emptyContainer;

        public FluidContainerData(FluidStack fluid, net.minecraft.ItemStack filledContainer, net.minecraft.ItemStack emptyContainer) {
            this.fluid = fluid;
            this.filledContainer = filledContainer;
            this.emptyContainer = emptyContainer;
        }
    }
}
