package net.rs.vulkanium.mixin.frapi;

import net.rs.vulkanium.client.render.frapi.VulkaniumRenderer;
import net.rs.vulkanium.client.render.frapi.wrapper.ExtendedMutableQuadViewImpl;
import net.rs.vulkanium.client.render.frapi.wrapper.MutableQuadViewWrapper;
import net.rs.vulkanium.client.render.model.AbstractBlockRenderContext;
import net.rs.vulkanium.client.render.model.MutableQuadViewImpl;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(BlockStateModelPart.class)
public interface BlockStateModelPartMixin extends FabricBlockStateModelPart {
    @Override
    default void emitQuads(QuadEmitter emitter, Predicate<@Nullable Direction> cullTest) {
        if (emitter instanceof MutableQuadViewWrapper wr && wr.getOriginal() instanceof AbstractBlockRenderContext.BlockEmitter be) {
            be.emitPart((BlockStateModelPart) this, cullTest, VulkaniumRenderer.BUFFERER);
        } else {
            FabricBlockStateModelPart.super.emitQuads(emitter, cullTest);
        }
    }
}
