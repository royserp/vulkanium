package net.rs.vulkanium.mixin.core.render.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.rs.vulkanium.client.util.FogParameters;
import net.rs.vulkanium.client.util.FogStorage;
import net.rs.vulkanium.client.util.GameRendererStorage;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class WorldGameRendererMixin implements GameRendererStorage {
    @Shadow
    @Final
    private FogRenderer fogRenderer;

    @Unique
    private final Matrix4f projectionMatrix = new Matrix4f();

    @Override
    public FogParameters vulkanium$getFogParameters() {
        return ((FogStorage) this.fogRenderer).vulkanium$getFogParameters();
    }

    @Override
    public Matrix4fc vulkanium$getProjectionMatrix() {
        return this.projectionMatrix;
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;", ordinal = 0))
    private GpuBufferSlice vulkanium$captureProjection(ProjectionMatrixBuffer instance, Matrix4f projectionMatrix, Operation<GpuBufferSlice> original) {
        this.projectionMatrix.set(projectionMatrix);
        return original.call(instance, projectionMatrix);
    }
}
