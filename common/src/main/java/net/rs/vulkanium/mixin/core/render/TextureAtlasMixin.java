package net.rs.vulkanium.mixin.core.render;

import net.rs.vulkanium.client.render.model.VulkaniumQuadAtlas;
import net.rs.vulkanium.client.render.texture.ExtendedTextureAtlas;
import net.rs.vulkanium.client.render.texture.VulkaniumSpriteFinder;
import net.rs.vulkanium.client.render.texture.VulkaniumSpriteFinderImpl;
import net.rs.vulkanium.client.render.texture.SpriteFinderCache;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin implements ExtendedTextureAtlas {
    @Shadow
    @Final
    private Identifier location;

    @Shadow
    private Map<Identifier, TextureAtlasSprite> texturesByName;

    @Shadow
    @Nullable
    private TextureAtlasSprite missingSprite;

    @Unique
    private boolean isBlocks = false;

    @Inject(method = "upload", at = @At("RETURN"))
    private void vulkanium$deleteSpriteFinder(SpriteLoader.Preparations preparations, CallbackInfo ci) {
        if (this.location.equals(TextureAtlas.LOCATION_BLOCKS)) {
            SpriteFinderCache.resetSpriteFinder();
            this.isBlocks = true;
        } else if (this.location.equals(TextureAtlas.LOCATION_ITEMS)) {
            SpriteFinderCache.resetItemSpriteFinder();
            this.isBlocks = false;
        }
    }

    @Override
    public VulkaniumSpriteFinder vulkanium$getSpriteFinder() {
        return new VulkaniumSpriteFinderImpl(this.texturesByName, this.missingSprite, isBlocks ? VulkaniumQuadAtlas.BLOCK : VulkaniumQuadAtlas.ITEM);
    }
}
