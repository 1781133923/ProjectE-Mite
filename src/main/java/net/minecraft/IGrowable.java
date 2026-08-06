package net.minecraft;

import java.util.Random;

/**
 * Shim for the 1.7.10 IGrowable crop interface. MITE's crop blocks do not
 * implement it, so instanceof checks return false; growth behavior may need
 * refinement per-block (see PORT.md).
 */
public interface IGrowable {
    boolean func_149851_a(World world, int x, int y, int z, boolean isRemote);

    boolean func_149852_a(World world, Random random, int x, int y, int z);

    void func_149853_b(World world, Random random, int x, int y, int z);
}
