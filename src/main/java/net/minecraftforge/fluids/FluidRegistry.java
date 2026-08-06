package net.minecraftforge.fluids;

import java.util.HashMap;
import java.util.Map;

public class FluidRegistry {
    public static final Fluid WATER = new Fluid("water").setDensity(1000).setViscosity(1000);
    public static final Fluid LAVA = new Fluid("lava").setDensity(3000).setViscosity(6000).setLuminosity(15);

    private static final Map<String, Fluid> FLUIDS = new HashMap<>();

    static {
        FLUIDS.put("water", WATER);
        FLUIDS.put("lava", LAVA);
    }

    public static Fluid getFluid(String fluidName) {
        return FLUIDS.get(fluidName);
    }

    public static void registerFluid(Fluid fluid) {
        FLUIDS.put(fluid.getName(), fluid);
    }
}
