package net.rs.vulkanium.mixin.features.render.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.rs.vulkanium.api.texture.SpriteUtil;
import net.rs.vulkanium.api.util.ColorABGR;
import net.rs.vulkanium.api.vertex.buffer.VertexBufferWriter;
import net.rs.vulkanium.client.model.quad.BakedQuadView;
import net.rs.vulkanium.client.render.immediate.model.BakedModelEncoder;
import net.rs.vulkanium.client.render.vertex.VertexConsumerUtils;
import net.rs.vulkanium.client.util.DirectionUtil;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
    @Unique
    private static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(() -> new SingleThreadedRandomSource(42L));

    @Unique
    private static final ThreadLocal<List<BlockStateModelPart>> LIST = ThreadLocal.withInitial(() -> new ObjectArrayList<>());

}
