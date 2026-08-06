package net.minecraftforge.fluids;

import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;

public class FluidStack {
    public final Fluid fluid;
    public int amount;
    public NBTTagCompound tag;

    public FluidStack(Fluid fluid, int amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public FluidStack(Fluid fluid, int amount, NBTTagCompound nbt) {
        this(fluid, amount);
        this.tag = nbt;
    }

    public Fluid getFluid() {
        return this.fluid;
    }

    public boolean isFluidEqual(FluidStack other) {
        return other != null && this.fluid == other.fluid;
    }

    public boolean isFluidEqual(ItemStack other) {
        return false;
    }

    public FluidStack copy() {
        FluidStack copy = new FluidStack(this.fluid, this.amount);
        if (this.tag != null) {
            copy.tag = (NBTTagCompound) this.tag.copy();
        }
        return copy;
    }
}
