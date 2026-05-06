package net.rs.vulkanium.client.render.chunk.vertex.format;

import net.rs.vulkanium.client.vk.attribute.VkVertexFormat;

public interface ChunkVertexType {
    VkVertexFormat getVertexFormat();

    ChunkVertexEncoder getEncoder();
}
