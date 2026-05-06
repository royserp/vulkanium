package net.rs.vulkanium.client.model.color.interop;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Block;

import java.util.List;

public interface BlockColorsExtension {
    static Reference2ReferenceMap<Block, List<BlockTintSource>> getProviders(BlockColors blockColors) {
        return ((BlockColorsExtension) blockColors).vulkanium$getProviders();
    }

    static ReferenceSet<Block> getOverridenVanillaBlocks(BlockColors blockColors) {
        return ((BlockColorsExtension) blockColors).vulkanium$getOverridenVanillaBlocks();
    }

    Reference2ReferenceMap<Block, List<BlockTintSource>> vulkanium$getProviders();

    ReferenceSet<Block> vulkanium$getOverridenVanillaBlocks();
}
