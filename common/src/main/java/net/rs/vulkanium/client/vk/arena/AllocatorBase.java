package net.rs.vulkanium.client.vk.arena;

import net.rs.vulkanium.client.vk.buffer.VkBuffer;

public interface AllocatorBase {
    long getDeviceUsedMemory();

    long getDeviceAllocatedMemory();

    void free(VkBufferSegment entry);

    boolean isEmpty();

    VkBuffer getBufferObject();
}
