package com.cricketcraft.chisel.api.carving;

import java.util.Collection;

public interface ICarvingGroup {
    String getName();

    String getOreName();

    Collection<ICarvingVariation> getVariations();
}
