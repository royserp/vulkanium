package net.rs.vulkanium.fabric.level;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.rs.vulkanium.client.model.color.ColorProviderRegistry;
import net.rs.vulkanium.client.model.light.LightPipelineProvider;
import net.rs.vulkanium.client.render.chunk.compile.pipeline.FluidRenderer;
import net.rs.vulkanium.client.services.PlatformLevelAccess;
import net.rs.vulkanium.client.world.LevelSlice;
import net.rs.vulkanium.client.world.VulkaniumAuxiliaryLightManager;
import net.rs.vulkanium.fabric.render.FluidRendererImpl;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Function;

public class FabricLevelAccess implements PlatformLevelAccess {
    @Override
    public @Nullable Object getBlockEntityData(BlockEntity blockEntity) {
        return blockEntity.getRenderData();
    }

    @Override
    public @Nullable VulkaniumAuxiliaryLightManager getLightManager(LevelChunk chunk, SectionPos pos) {
        return null;
    }
}
