package net.rs.vulkanium.mixin.core.model.colors;

import it.unimi.dsi.fastutil.objects.*;
import net.rs.vulkanium.client.VulkaniumClientMod;
import net.rs.vulkanium.client.model.color.interop.BlockColorsExtension;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BlockColors.class)
public class BlockColorsMixin implements BlockColorsExtension {
    // We're keeping a copy as we need to be able to iterate over the entry pairs, rather than just the values.
    @Unique
    private final Reference2ReferenceMap<Block, List<BlockTintSource>> blocksToColor = new Reference2ReferenceOpenHashMap<>();

    @Unique
    private final ReferenceSet<Block> overridenBlocks = new ReferenceOpenHashSet<>();

    @Inject(method = "register", at = @At("HEAD"))
    private void preRegisterColorProvider(List<BlockTintSource> provider, Block[] blocks, CallbackInfo ci) {
        for (Block block : blocks) {
            // There will be one provider already registered for vanilla blocks, if we are replacing it,
            // it means a mod is using custom logic, and we need to disable per-vertex coloring
            if (this.blocksToColor.put(block, provider) != null) {
                this.overridenBlocks.add(block);
                VulkaniumClientMod.logger().info("Block {} had its color provider replaced with {} and will not use per-vertex coloring", BuiltInRegistries.BLOCK.getKey(block), provider.toString());
            }
        }
    }

    @Override
    public Reference2ReferenceMap<Block, List<BlockTintSource>> vulkanium$getProviders() {
        return Reference2ReferenceMaps.unmodifiable(this.blocksToColor);
    }

    @Override
    public ReferenceSet<Block> vulkanium$getOverridenVanillaBlocks() {
        return ReferenceSets.unmodifiable(this.overridenBlocks);
    }
}
