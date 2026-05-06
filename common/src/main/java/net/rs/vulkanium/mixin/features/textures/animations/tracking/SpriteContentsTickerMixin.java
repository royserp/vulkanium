package net.rs.vulkanium.mixin.features.textures.animations.tracking;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.rs.vulkanium.client.VulkaniumClientMod;
import net.rs.vulkanium.client.render.texture.SpriteContentsExtension;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SpriteContents.AnimationState.class)
public class SpriteContentsTickerMixin {
    @Shadow
    @Final
    private SpriteContents.AnimatedTexture animationInfo;
    @Shadow
    private int frame;
    @Unique
    private SpriteContents parent;

    @Unique
    private boolean hasUploadedAllOnce = false;

    /**
     * @author IMS
     * @reason Replace fragile Shadow
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    public void assignParent(SpriteContents spriteContents, SpriteContents.AnimatedTexture animatedTexture, Int2ObjectMap int2ObjectMap, GpuBufferSlice[] gpuBufferSlices, CallbackInfo ci) {
        this.parent = spriteContents;
    }

    @Inject(method = "needsToDraw", at = @At("HEAD"), cancellable = true)
    private void preTick(CallbackInfoReturnable<Boolean> cir) {
        SpriteContentsExtension parent = (SpriteContentsExtension) this.parent;

        boolean onDemand = VulkaniumClientMod.options().performance.animateOnlyVisibleTextures;

        if (!hasUploadedAllOnce) {
            if (this.frame == this.animationInfo.frames.size() - 1) {
                hasUploadedAllOnce = true;
            } else {
                return;
            }
        }

        if (onDemand && !parent.vulkanium$isActive()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "drawToAtlas", at = @At("TAIL"))
    private void postTick(CallbackInfo ci) {
        SpriteContentsExtension parent = (SpriteContentsExtension) this.parent;
        parent.vulkanium$setActive(false);
    }
}
