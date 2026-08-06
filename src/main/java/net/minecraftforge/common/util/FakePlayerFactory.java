package net.minecraftforge.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.World;

import java.util.UUID;

public class FakePlayerFactory {
    private static FakePlayer player;
    private static UUID lastProfileId;

    public static FakePlayer get(World world, GameProfile profile) {
        if (player == null || lastProfileId != profile.getId()) {
            player = new FakePlayer(world);
            lastProfileId = profile.getId();
        }
        return player;
    }
}
