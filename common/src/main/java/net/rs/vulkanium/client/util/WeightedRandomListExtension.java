package net.rs.vulkanium.client.util;

import net.minecraft.util.RandomSource;

public interface WeightedRandomListExtension<T> {
    T vulkanium$getQuick(RandomSource random);
}
