package net.rs.vulkanium.client.render.chunk;

import java.util.function.Predicate;

import net.rs.vulkanium.api.blockentity.BlockEntityRenderHandler;
import net.rs.vulkanium.api.blockentity.BlockEntityRenderPredicate;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityRenderHandlerImpl implements BlockEntityRenderHandler {
    @Override
    public <T extends BlockEntity> void addRenderPredicate(BlockEntityType<T> type, BlockEntityRenderPredicate<T> predicate) {
        ExtendedBlockEntityType.addRenderPredicate(type, predicate);
    }

    @Override
    public <T extends BlockEntity> boolean removeRenderPredicate(BlockEntityType<T> type, BlockEntityRenderPredicate<T> predicate) {
        return ExtendedBlockEntityType.removeRenderPredicate(type, predicate);
    }
}
