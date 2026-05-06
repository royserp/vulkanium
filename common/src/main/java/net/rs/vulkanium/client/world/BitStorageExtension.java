package net.rs.vulkanium.client.world;

import net.minecraft.world.level.chunk.Palette;

public interface BitStorageExtension {
    <T> void vulkanium$unpack(T[] out, Palette<T> palette);
}
