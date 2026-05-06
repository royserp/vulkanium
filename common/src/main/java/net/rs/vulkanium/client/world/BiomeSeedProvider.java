package net.rs.vulkanium.client.world;

import net.minecraft.client.multiplayer.ClientLevel;

public interface BiomeSeedProvider {
    static long getBiomeZoomSeed(ClientLevel level) {
        return ((BiomeSeedProvider) level).vulkanium$getBiomeZoomSeed();
    }

    long vulkanium$getBiomeZoomSeed();
}
