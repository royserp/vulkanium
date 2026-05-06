package net.rs.vulkanium.mixin.features.render.immediate.buffer_builder.intrinsics;

import com.mojang.blaze3d.vertex.*;
import net.rs.vulkanium.api.texture.SpriteUtil;
import net.rs.vulkanium.api.util.ColorABGR;
import net.rs.vulkanium.api.vertex.buffer.VertexBufferWriter;
import net.rs.vulkanium.client.model.quad.ModelQuadView;
import net.rs.vulkanium.client.render.immediate.model.BakedModelEncoder;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings({ "SameParameterValue" })
@Mixin(BufferBuilder.class)
public abstract class FastBakedQuadBufferBuilderMixin implements VertexConsumer {
    @Shadow
    @Final
    private boolean blockFormat;

    @Shadow
    @Final
    private boolean entityFormat;

    @Override
    public void putBakedQuad(PoseStack.Pose pose, BakedQuad bakedQuad, QuadInstance instance) {
        if (!this.blockFormat) {
            VertexConsumer.super.putBakedQuad(pose, bakedQuad, instance);

            if (bakedQuad.materialInfo().sprite() != null) {
                SpriteUtil.INSTANCE.markSpriteActive(bakedQuad.materialInfo().sprite());
            }

            return;
        }

        VertexBufferWriter writer = VertexBufferWriter.of(this);

        ModelQuadView quad = (ModelQuadView) (Object) bakedQuad;

        BakedModelEncoder.writeQuadVertices(writer, pose, quad, instance, this.entityFormat);

        if (quad.getSprite() != null) {
            SpriteUtil.INSTANCE.markSpriteActive(quad.getSprite());
        }
    }
}
