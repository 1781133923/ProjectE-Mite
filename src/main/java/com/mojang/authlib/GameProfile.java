package com.mojang.authlib;

import java.util.UUID;

public class GameProfile {
    private final UUID id;
    private final String name;

    public GameProfile(UUID id, String name) {
        if (id == null && name == null) {
            throw new IllegalArgumentException("Name and ID cannot both be blank");
        }
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "GameProfile{id=" + this.id + ", name='" + this.name + "'}";
    }
}
