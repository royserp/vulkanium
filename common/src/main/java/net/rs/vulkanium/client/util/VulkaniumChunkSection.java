package net.rs.vulkanium.client.util;

import net.rs.vulkanium.client.render.VulkaniumWorldRenderer;
import net.rs.vulkanium.client.render.chunk.ChunkRenderMatrices;

public interface VulkaniumChunkSection {
    void vulkanium$setRendering(VulkaniumWorldRenderer renderer, ChunkRenderMatrices matrices, double x, double y, double z);
}
