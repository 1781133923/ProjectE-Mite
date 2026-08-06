package net.minecraftforge.fluids;

public class Fluid {
    private final String name;
    private int luminosity;
    private int density;
    private int temperature;
    private int viscosity;

    public Fluid(String fluidName) {
        this.name = fluidName;
    }

    public String getName() {
        return this.name;
    }

    public Fluid setLuminosity(int luminosity) {
        this.luminosity = luminosity;
        return this;
    }

    public int getLuminosity() {
        return this.luminosity;
    }

    public Fluid setDensity(int density) {
        this.density = density;
        return this;
    }

    public int getDensity() {
        return this.density;
    }

    public Fluid setTemperature(int temperature) {
        this.temperature = temperature;
        return this;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public Fluid setViscosity(int viscosity) {
        this.viscosity = viscosity;
        return this;
    }

    public int getViscosity() {
        return this.viscosity;
    }
}
