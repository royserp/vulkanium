package net.rs.vulkanium.mixin.core.render.world;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.rs.vulkanium.client.render.VulkaniumWorldRenderer;
import net.rs.vulkanium.client.render.chunk.ChunkRenderMatrices;
import net.rs.vulkanium.client.util.VulkaniumChunkSection;
import net.rs.vulkanium.client.vk.renderpass.VulkanRenderPassExtension;
import net.rs.vulkanium.mixin.core.render.RenderPassAccessor;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.OptionalDouble;
import java.util.OptionalInt;

@Mixin(ChunkSectionsToRender.class)
public abstract class ChunkSectionsToRenderMixin implements VulkaniumChunkSection {
    @Shadow @Final private GpuTextureView textureView;

    @Unique
    private VulkaniumWorldRenderer renderer;

    @Unique
    private ChunkRenderMatrices matrices;

    @Unique
    private double x;

    @Unique
    private double y;

    @Unique
    private double z;

    /**
     * @reason Redirect to our renderer using the native RenderPass system.
     * @author JellySquid
     */
    @Overwrite
    public void renderGroup(final ChunkSectionLayerGroup group, final GpuSampler sampler) {
        if (this.renderer == null) {
            return;
        }

        RenderTarget renderTarget = group.outputTarget();

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> "Section layers for " + group.label(),
                        renderTarget.getColorTextureView(),
                        OptionalInt.empty(),
                        renderTarget.getDepthTextureView(),
                        OptionalDouble.empty()
                )) {
            
            VkCommandBuffer commandBuffer = ((VulkanRenderPassExtension) ((RenderPassAccessor) renderPass).getBackend()).vulkanium$getCommandBuffer();
            this.renderer.drawChunkLayer(group, this.matrices, this.x, this.y, this.z, sampler, commandBuffer);
        }
    }

    @Override
    public void vulkanium$setRendering(VulkaniumWorldRenderer renderer, ChunkRenderMatrices matrices, double x, double y, double z) {
        this.renderer = renderer;
        this.matrices = matrices;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
