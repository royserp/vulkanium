package net.rs.vulkanium.client.render.chunk.map;

import net.minecraft.client.multiplayer.ClientLevel;

public interface ChunkTrackerHolder {
    static ChunkTracker get(ClientLevel level) {
        return ((ChunkTrackerHolder) level).vulkanium$getTracker();
    }

    ChunkTracker vulkanium$getTracker();
}
