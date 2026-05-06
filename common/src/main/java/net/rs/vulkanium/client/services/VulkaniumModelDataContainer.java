package net.rs.vulkanium.client.services;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;

import java.util.Map;

/**
 * A container that holds the platform's model data.
 */
public class VulkaniumModelDataContainer {
    private final Long2ObjectMap<VulkaniumModelData> modelDataMap;
    private final boolean isEmpty;

    public VulkaniumModelDataContainer(Long2ObjectMap<VulkaniumModelData> modelDataMap) {
        this.modelDataMap = modelDataMap;
        this.isEmpty = modelDataMap.isEmpty();
    }

    public VulkaniumModelData getModelData(BlockPos pos) {
        return modelDataMap.getOrDefault(pos.asLong(), VulkaniumModelData.EMPTY);
    }

    public boolean isEmpty() {
        return isEmpty;
    }
}
