package net.rs.vulkanium.fabric.render;

import net.rs.vulkanium.client.model.color.ColorProviderRegistry;
import net.rs.vulkanium.client.model.light.LightPipelineProvider;
import net.rs.vulkanium.client.model.quad.blender.BlendedColorProvider;
import net.rs.vulkanium.client.render.chunk.compile.ChunkBuildBuffers;
import net.rs.vulkanium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.rs.vulkanium.client.render.chunk.compile.pipeline.FluidRenderer;
import net.rs.vulkanium.client.services.FluidRendererFactory;
import net.rs.vulkanium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class FluidRendererImpl extends FluidRenderer {
    @Override
    public void render(LevelSlice level, BlockState blockState, FluidState fluidState, BlockPos blockPos, BlockPos offset, TranslucentGeometryCollector collector, ChunkBuildBuffers buffers) {
        // No-op
    }

    public static class FabricFactory implements FluidRendererFactory {
        @Override
        public FluidRenderer createPlatformFluidRenderer(ColorProviderRegistry colorRegistry, LightPipelineProvider lightPipelineProvider) {
            return new FluidRendererImpl();
        }

        @Override
        public BlendedColorProvider<FluidState> getWaterColorProvider() {
            return null;
        }

        @Override
        public BlendedColorProvider<BlockState> getWaterBlockColorProvider() {
            return null;
        }
    }
}
