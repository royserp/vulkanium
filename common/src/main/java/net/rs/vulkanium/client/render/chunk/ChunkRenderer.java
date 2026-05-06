package net.rs.vulkanium.client.render.chunk;

import com.mojang.blaze3d.textures.GpuSampler;
import net.rs.vulkanium.client.vk.device.CommandList;
import net.rs.vulkanium.client.render.chunk.lists.ChunkRenderListIterable;
import net.rs.vulkanium.client.render.chunk.terrain.TerrainRenderPass;
import net.rs.vulkanium.client.render.viewport.CameraTransform;
import net.rs.vulkanium.client.util.FogParameters;
import net.rs.vulkanium.client.vk.renderpass.VulkanRenderPass;

/**
 * The chunk render backend takes care of managing the graphics resource state of chunk render containers. This includes
 * the handling of uploading their data to the graphics card and rendering responsibilities.
 */
public interface ChunkRenderer {
    /**
     * Renders the given chunk render list to the active framebuffer.
     *
     * @param matrices                The camera matrices to use for rendering
     * @param commandList             The command list which OpenGL commands should be serialized to
     * @param renderLists             The collection of render lists
     * @param pass                    The block render pass to execute
     * @param camera                  The camera context containing chunk offsets for the current render
     * @param parameters              The current fog state
     * @param indexedRenderingEnabled Whether indexed rendering is enabled
     * @param terrainSampler          The sampler to use for the atlas
     * @param externalPass            An optional external render pass to use instead of starting a new one
     */
    void render(ChunkRenderMatrices matrices, CommandList commandList, ChunkRenderListIterable renderLists, TerrainRenderPass pass, CameraTransform camera, FogParameters parameters, boolean indexedRenderingEnabled, GpuSampler terrainSampler, VulkanRenderPass externalPass);

    default void render(ChunkRenderMatrices matrices, CommandList commandList, ChunkRenderListIterable renderLists, TerrainRenderPass pass, CameraTransform camera, FogParameters parameters, boolean indexedRenderingEnabled, GpuSampler terrainSampler) {
        this.render(matrices, commandList, renderLists, pass, camera, parameters, indexedRenderingEnabled, terrainSampler, null);
    }

    /**
     * Deletes this render backend and any resources attached to it.
     */
    void delete(CommandList commandList);
}
