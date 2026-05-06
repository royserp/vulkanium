package net.rs.vulkanium.client.render.chunk.compile.pipeline;

import net.rs.vulkanium.client.model.color.ColorProviderRegistry;
import net.rs.vulkanium.client.model.light.LightPipelineProvider;
import net.rs.vulkanium.client.render.chunk.compile.ChunkBuildBuffers;
import net.rs.vulkanium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.rs.vulkanium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public abstract class FluidRenderer {
    public abstract void render(LevelSlice level, BlockState blockState, FluidState fluidState, BlockPos blockPos, BlockPos offset, TranslucentGeometryCollector collector, ChunkBuildBuffers buffers);
}
