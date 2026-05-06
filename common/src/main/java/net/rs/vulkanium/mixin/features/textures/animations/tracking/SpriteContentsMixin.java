package net.rs.vulkanium.mixin.features.textures.animations.tracking;

import net.rs.vulkanium.client.render.texture.SpriteContentsExtension;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin implements SpriteContentsExtension {
    @Shadow
    @Final
    @Nullable
    private SpriteContents.AnimatedTexture animatedTexture;

    @Unique
    private boolean active;

    @Override
    public void vulkanium$setActive(boolean value) {
        this.active = value;
    }

    @Override
    public boolean vulkanium$hasAnimation() {
        return this.animatedTexture != null;
    }

    @Override
    public boolean vulkanium$isActive() {
        return this.active;
    }
}
