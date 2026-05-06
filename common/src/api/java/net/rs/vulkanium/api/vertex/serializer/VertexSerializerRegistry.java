package net.rs.vulkanium.api.vertex.serializer;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.rs.vulkanium.api.internal.DependencyInjection;

public interface VertexSerializerRegistry {
    VertexSerializerRegistry INSTANCE = DependencyInjection.load(VertexSerializerRegistry.class,
            "net.rs.vulkanium.client.render.vertex.serializers.VertexSerializerRegistryImpl");

    static VertexSerializerRegistry instance() {
        return INSTANCE;
    }

    VertexSerializer get(VertexFormat srcFormat, VertexFormat dstFormat);

    void registerSerializer(VertexFormat srcFormat, VertexFormat dstFormat, VertexSerializer serializer);
}
