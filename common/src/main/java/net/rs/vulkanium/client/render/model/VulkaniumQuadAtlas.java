package net.rs.vulkanium.client.render.model;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

public enum VulkaniumQuadAtlas {
    BLOCK,
    ITEM;

    public static VulkaniumQuadAtlas of(Identifier atlasTextureId) {
        if (atlasTextureId.equals(TextureAtlas.LOCATION_BLOCKS)) {
            return BLOCK;
        } else if (atlasTextureId.equals(TextureAtlas.LOCATION_ITEMS)) {
            return ITEM;
        } else {
            return null;
        }
    }
}
