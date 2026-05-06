package net.rs.vulkanium.client.vk.device;

import net.rs.vulkanium.client.vk.buffer.VkIndexType;

public interface DrawCommandList extends AutoCloseable {
    void multiDrawElementsBaseVertex(MultiDrawBatch batch, VkIndexType indexType);

    void endTessellating();

    void flush();

    @Override
    default void close() {
        this.flush();
    }
}
