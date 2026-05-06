package net.rs.vulkanium.client.render.chunk.shader;

import com.mojang.blaze3d.textures.GpuSampler;
import net.rs.vulkanium.client.render.chunk.terrain.TerrainRenderPass;
import net.rs.vulkanium.client.util.FogParameters;
import net.rs.vulkanium.client.vk.buffer.VkBuffer;
import org.joml.Matrix4fc;

public interface ChunkShaderInterface {
    @Deprecated
    void setupState(TerrainRenderPass pass, FogParameters parameters, GpuSampler terrainSampler);

    @Deprecated
    void resetState();

    void fillPushConstants(long src);

    void setProjectionMatrix(Matrix4fc matrix);

    void setModelViewMatrix(Matrix4fc matrix);

    void setRegionOffset(float x, float y, float z);

    void setChunkData(VkBuffer buffer, int time);
}
