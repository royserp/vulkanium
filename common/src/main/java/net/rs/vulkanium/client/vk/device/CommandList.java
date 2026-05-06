package net.rs.vulkanium.client.vk.device;

import net.rs.vulkanium.client.vk.buffer.VkBuffer;
import net.rs.vulkanium.client.vk.buffer.VkBufferUsages;
import net.rs.vulkanium.client.vk.buffer.VkMappingType;
import net.rs.vulkanium.client.vk.buffer.VkMapping;
import net.rs.vulkanium.client.vk.fence.VkFence;
import net.rs.vulkanium.client.vk.renderpass.VulkanRenderPass;
import net.rs.vulkanium.client.vk.util.EnumBitField;

public interface CommandList extends AutoCloseable {
    VkBuffer createBuffer(long bufferSize, VkMappingType mappingType, EnumBitField<VkBufferUsages> flags);

    void copyBufferToBuffer(VkBuffer src, VkBuffer dst, long readOffset, long writeOffset, long bytes);

    void deleteBuffer(VkBuffer buffer);

    void flush();

    @Override
    default void close() {
        this.flush();
    }

    VkMapping mapBuffer(VkBuffer buffer, long offset, long length);

    void unmap(VkMapping map);

    void flushMappedRange(VkMapping map, int offset, int length);

    VkFence createFence();

    VulkanRenderPass startRenderPass(long colorTextureView);
}
