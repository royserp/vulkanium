package net.rs.vulkanium.api.vertex.serializer;

public interface VertexSerializer {
    void serialize(long srcBuffer, long dstBuffer, int count);
}
