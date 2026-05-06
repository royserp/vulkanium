package net.rs.vulkanium.mixin.core.render;

import java.util.function.Predicate;

import net.rs.vulkanium.api.blockentity.BlockEntityRenderPredicate;
import net.rs.vulkanium.client.render.chunk.ExtendedBlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin<T extends BlockEntity> implements ExtendedBlockEntityType<T> {
    @Unique
    private BlockEntityRenderPredicate<T>[] vulkanium$renderPredicates = new BlockEntityRenderPredicate[0];

    @Override
    public BlockEntityRenderPredicate<T>[] vulkanium$getRenderPredicates() {
        return vulkanium$renderPredicates;
    }

    @Override
    public void vulkanium$addRenderPredicate(BlockEntityRenderPredicate<T> predicate) {
        vulkanium$renderPredicates = ArrayUtils.add(vulkanium$renderPredicates, predicate);
    }

    @Override
    public boolean vulkanium$removeRenderPredicate(BlockEntityRenderPredicate<T> predicate) {
        int index = ArrayUtils.indexOf(vulkanium$renderPredicates, predicate);

        if (index == ArrayUtils.INDEX_NOT_FOUND) {
            return false;
        }

        vulkanium$renderPredicates = ArrayUtils.remove(vulkanium$renderPredicates, index);
        return true;
    }
}
