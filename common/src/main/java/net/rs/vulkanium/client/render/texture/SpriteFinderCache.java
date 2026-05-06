package net.rs.vulkanium.client.render.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;

/**
 * Caches {@link SpriteFinder}s for maximum efficiency. They must be refreshed after each resource reload.
 *
 * <p><b>This class should not be used during a resource reload</b>, as returned SpriteFinders may be null or outdated.
 */
public class SpriteFinderCache {
    private static VulkaniumSpriteFinder blockAtlasSpriteFinder;
    private static VulkaniumSpriteFinder itemAtlasSpriteFinder;

    public static VulkaniumSpriteFinder forBlockAtlas() {
        if (blockAtlasSpriteFinder == null) {
            blockAtlasSpriteFinder = ((ExtendedTextureAtlas) Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)).vulkanium$getSpriteFinder();
        }

        return blockAtlasSpriteFinder;
    }

    public static VulkaniumSpriteFinder forItemAtlas() {
        if (itemAtlasSpriteFinder == null) {
            itemAtlasSpriteFinder = ((ExtendedTextureAtlas) Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS)).vulkanium$getSpriteFinder();
        }

        return itemAtlasSpriteFinder;
    }

    public static void resetSpriteFinder() {
        blockAtlasSpriteFinder = null;
    }

    public static void resetItemSpriteFinder() {
        itemAtlasSpriteFinder = null;
    }
}
