package net.rs.vulkanium.neoforge.level;

import net.rs.vulkanium.client.services.PlatformLevelAccess;
import net.rs.vulkanium.client.world.VulkaniumAuxiliaryLightManager;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

public class NeoForgeLevelAccess implements PlatformLevelAccess {
    @Override
    public @Nullable Object getBlockEntityData(BlockEntity blockEntity) {
        return null; // NeoForge does not have an equivalent API.
    }

    @Override
    public @Nullable VulkaniumAuxiliaryLightManager getLightManager(LevelChunk chunk, SectionPos pos) {
        return (VulkaniumAuxiliaryLightManager) chunk.getAuxLightManager(pos.origin());
    }
}
