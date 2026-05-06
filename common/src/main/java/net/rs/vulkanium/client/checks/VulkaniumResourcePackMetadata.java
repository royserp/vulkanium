package net.rs.vulkanium.client.checks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.server.packs.metadata.MetadataSectionType;

/**
 * Reads additional metadata for Vulkanium from a resource pack's `pack.mcmeta` file. This allows the
 * resource pack author to specify which shaders from their pack are not usable with Vulkanium, but that
 * the author is aware of and is fine with being ignored.
 */
public record VulkaniumResourcePackMetadata(List<String> ignoredShaders) {
    public static final Codec<VulkaniumResourcePackMetadata> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(Codec.STRING.listOf().fieldOf("ignored_shaders")
                    .forGetter(VulkaniumResourcePackMetadata::ignoredShaders))
                    .apply(instance, VulkaniumResourcePackMetadata::new)
    );
    public static final MetadataSectionType<VulkaniumResourcePackMetadata> SERIALIZER =
            new MetadataSectionType<>("vulkanium", CODEC);
}
