package com.cricketcraft.chisel.api.carving;

import java.util.Collection;

public interface ICarvingRegistry {
    Collection<String> getSortedGroupNames();

    ICarvingGroup getGroup(String name);
}
