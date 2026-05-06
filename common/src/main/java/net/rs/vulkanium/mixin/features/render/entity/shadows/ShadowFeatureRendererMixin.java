package net.rs.vulkanium.mixin.features.render.entity.shadows;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.rs.vulkanium.api.util.NormI8;
import net.rs.vulkanium.client.render.vertex.VertexConsumerUtils;
import net.rs.vulkanium.api.vertex.buffer.VertexBufferWriter;
import net.rs.vulkanium.api.vertex.format.common.EntityVertex;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ShadowFeatureRenderer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.rs.vulkanium.api.util.ColorABGR;
import net.rs.vulkanium.api.math.MatrixHelper;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ShadowFeatureRenderer.class)
public abstract class ShadowFeatureRendererMixin extends RenderTypeFeatureRenderer<ShadowFeatureRenderer.Submit> {
    @Unique
    private static final int DEFAULT_NORMAL = NormI8.pack(0.0f, 1.0f, 0.0f);

    @Shadow
    @Final
    private static RenderType SHADOW_RENDER_TYPE;

    @Unique
    private static final int SHADOW_COLOR = ColorABGR.pack(1.0f, 1.0f, 1.0f);

    /**
     * @author JellySquid
     * @reason Reduce vertex assembly overhead for shadow rendering
     */
    @Override
    protected void buildGroup(final FeatureFrameContext context, final List<ShadowFeatureRenderer.Submit> submits) {
        VertexConsumer vertices = this.getVertexBuilder(SHADOW_RENDER_TYPE);

        var writer = VertexConsumerUtils.convertOrLog(vertices);

        if (writer == null) {
            // Fallback to Vanilla logic if we can't get a writer
            for (ShadowFeatureRenderer.Submit submit : submits) {
                this.prepare(submit, vertices);
            }
            return;
        }

        for (ShadowFeatureRenderer.Submit submit : submits) {
            Matrix4fc matrices = submit.pose();

            for (EntityRenderState.ShadowPiece shadowPiece : submit.pieces()) {
                float alpha = shadowPiece.alpha();

                if (alpha >= 0.0F) {
                    if (alpha > 1.0F) {
                        alpha = 1.0F;
                    }

                    AABB box = shadowPiece.shapeBelow().bounds();

                    float minX = (float) (shadowPiece.relativeX() + box.minX);
                    float maxX = (float) (shadowPiece.relativeX() + box.maxX);
                    float minY = (float) (shadowPiece.relativeY() + box.minY);
                    float minZ = (float) (shadowPiece.relativeZ() + box.minZ);
                    float maxZ = (float) (shadowPiece.relativeZ() + box.maxZ);

                    renderShadowPart(matrices, writer, submit.radius(), alpha, minX, maxX, minY, minZ, maxZ);
                }
            }
        }
    }

    @Shadow
    protected abstract void prepare(ShadowFeatureRenderer.Submit submit, VertexConsumer builder);

    @Unique
    private static void renderShadowPart(Matrix4fc matPosition, VertexBufferWriter writer, float radius, float alpha, float minX, float maxX, float minY, float minZ, float maxZ) {
        float size = 0.5F * (1.0F / radius);

        float u1 = (-minX * size) + 0.5F;
        float u2 = (-maxX * size) + 0.5F;

        float v1 = (-minZ * size) + 0.5F;
        float v2 = (-maxZ * size) + 0.5F;


        var color = ColorABGR.withAlpha(SHADOW_COLOR, alpha);
        var normal = DEFAULT_NORMAL; // This seems wrong, but it is identical to Vanilla's handling.

        try (MemoryStack stack = MemoryStack.stackPush()) {
            long buffer = stack.nmalloc(4 * EntityVertex.STRIDE);
            long ptr = buffer;

            writeShadowVertex(ptr, matPosition, minX, minY, minZ, u1, v1, color, normal);
            ptr += EntityVertex.STRIDE;

            writeShadowVertex(ptr, matPosition, minX, minY, maxZ, u1, v2, color, normal);
            ptr += EntityVertex.STRIDE;

            writeShadowVertex(ptr, matPosition, maxX, minY, maxZ, u2, v2, color, normal);
            ptr += EntityVertex.STRIDE;

            writeShadowVertex(ptr, matPosition, maxX, minY, minZ, u2, v1, color, normal);
            ptr += EntityVertex.STRIDE;

            writer.push(stack, buffer, 4, EntityVertex.FORMAT);
        }
    }

    @Unique
    private static void writeShadowVertex(long ptr, Matrix4fc matPosition, float x, float y, float z, float u, float v, int color, int normal) {
        // The transformed position vector
        float xt = MatrixHelper.transformPositionX(matPosition, x, y, z);
        float yt = MatrixHelper.transformPositionY(matPosition, x, y, z);
        float zt = MatrixHelper.transformPositionZ(matPosition, x, y, z);

        EntityVertex.write(ptr, xt, yt, zt, color, u, v, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, normal);
    }
}
