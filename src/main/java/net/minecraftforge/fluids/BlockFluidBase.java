package net.minecraftforge.fluids;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.Material;

public abstract class BlockFluidBase extends Block {
    protected Fluid fluid;

    protected BlockFluidBase(int id, Material material, BlockConstants constants) {
        super(id, material, constants);
    }

    public Fluid getFluid() {
        return this.fluid;
    }
}
