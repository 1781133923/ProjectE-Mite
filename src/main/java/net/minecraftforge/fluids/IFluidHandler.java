package net.minecraftforge.fluids;

import net.minecraftforge.common.util.ForgeDirection;

public interface IFluidHandler {
    default int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return 0;
    }

    default FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return null;
    }

    default FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return null;
    }

    default boolean canFill(ForgeDirection from, Fluid fluid) {
        return false;
    }

    default boolean canDrain(ForgeDirection from, Fluid fluid) {
        return false;
    }

    default FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[0];
    }
}
