package net.rs.vulkanium.neoforge.block;

import net.rs.vulkanium.api.util.NormI8;
import net.rs.vulkanium.client.model.quad.ModelQuadView;
import net.rs.vulkanium.client.render.model.AmbientOcclusionMode;
import net.rs.vulkanium.client.services.PlatformBlockAccess;
import net.rs.vulkanium.client.util.DirectionUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class NeoForgeBlockAccess implements PlatformBlockAccess {
    @Override
    public int getLightEmission(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return state.getLightEmission(level, pos);
    }

    @Override
    public boolean shouldSkipRender(BlockGetter level, BlockState selfState, BlockState otherState, BlockPos selfPos, BlockPos otherPos, Direction facing) {
        return selfState.supportsExternalFaceHiding() && (otherState.hidesNeighborFace(level, otherPos, selfState, DirectionUtil.getOpposite(facing)));
    }

    @Override
    public boolean shouldShowFluidOverlay(BlockState block, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return block.shouldDisplayFluidOverlay(level, pos, fluidState);
    }

    @Override
    public boolean platformHasBlockData() {
        return true;
    }

    @Override
    public float getNormalVectorShade(ModelQuadView quad, BlockAndTintGetter level, boolean shade) {
        return level.getShade(NormI8.unpackX(quad.getFaceNormal()), NormI8.unpackY(quad.getFaceNormal()), NormI8.unpackZ(quad.getFaceNormal()), shade);
    }

    @Override
    public AmbientOcclusionMode usesAmbientOcclusion(BlockStateModelPart model, BlockState state, ChunkSectionLayer renderType, BlockAndTintGetter level, BlockPos pos) {
        return switch (model.ambientOcclusion()) {
            case TRUE -> AmbientOcclusionMode.ENABLED;
            case FALSE -> AmbientOcclusionMode.DISABLED;
            case DEFAULT -> AmbientOcclusionMode.DEFAULT;
        };
    }

    @Override
    public boolean shouldBlockEntityGlow(BlockEntity blockEntity, LocalPlayer player) {
        return blockEntity.hasCustomOutlineRendering(player);
    }

    @Override
    public boolean shouldOccludeFluid(Direction adjDirection, BlockState adjBlockState, FluidState fluid) {
        return adjBlockState.shouldHideAdjacentFluidFace(adjDirection, fluid);
    }
}
