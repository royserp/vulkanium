package net.rs.vulkanium.api.vertex.format;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.rs.vulkanium.api.internal.DependencyInjection;

public interface VertexFormatRegistry {
    VertexFormatRegistry INSTANCE = DependencyInjection.load(VertexFormatRegistry.class,
            "net.rs.vulkanium.client.render.vertex.VertexFormatRegistryImpl");

    static VertexFormatRegistry instance() {
        return INSTANCE;
    }

    int allocateGlobalId(VertexFormat format);
}