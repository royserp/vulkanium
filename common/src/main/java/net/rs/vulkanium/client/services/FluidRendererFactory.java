package net.rs.vulkanium.client.services;

import net.rs.vulkanium.client.model.color.ColorProviderRegistry;
import net.rs.vulkanium.client.model.light.LightPipelineProvider;
import net.rs.vulkanium.client.model.quad.blender.BlendedColorProvider;
import net.rs.vulkanium.client.render.chunk.compile.pipeline.FluidRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface FluidRendererFactory {
    FluidRendererFactory INSTANCE = Services.load(FluidRendererFactory.class);

    static FluidRendererFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a new platform dependent fluid renderer.
     * @param colorRegistry The current color registry.
     * @param lightPipelineProvider The current {@code LightPipelineProvider}.
     * @return A new fluid renderer.
     */
    FluidRenderer createPlatformFluidRenderer(ColorProviderRegistry colorRegistry, LightPipelineProvider lightPipelineProvider);

    BlendedColorProvider<FluidState> getWaterColorProvider();

    BlendedColorProvider<BlockState> getWaterBlockColorProvider();
}
