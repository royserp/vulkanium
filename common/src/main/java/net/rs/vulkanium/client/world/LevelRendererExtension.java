package net.rs.vulkanium.client.world;

import net.rs.vulkanium.client.render.VulkaniumWorldRenderer;
import net.rs.vulkanium.client.render.chunk.ChunkRenderMatrices;

public interface LevelRendererExtension {
    VulkaniumWorldRenderer vulkanium$getWorldRenderer();

    /**
     * Hook for mods to change the matrices.
     * @param matrices The new chunk matrices.
     */
    void vulkanium$setMatrices(ChunkRenderMatrices matrices);

    ChunkRenderMatrices vulkanium$getMatrices();
}
