package net.rs.vulkanium.client.vk.device;

import net.rs.vulkanium.client.compatibility.environment.OsUtils;
import net.rs.vulkanium.client.vk.Blaze3DAccess;
import net.rs.vulkanium.client.vk.VkObjectDestroyable;
import net.rs.vulkanium.client.vk.buffer.VkBuffer;
import net.rs.vulkanium.client.vk.buffer.VkBufferUsages;
import net.rs.vulkanium.client.vk.buffer.VkMapping;
import net.rs.vulkanium.client.vk.buffer.VkMappingType;
import net.rs.vulkanium.client.vk.fence.VkFence;
import net.rs.vulkanium.client.vk.fence.VkFenceQueue;
import net.rs.vulkanium.client.vk.functions.DeviceFunctions;
import net.rs.vulkanium.client.vk.renderpass.VulkanRenderPass;
import net.rs.vulkanium.client.vk.util.EnumBitField;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

public class VKRenderDevice implements RenderDevice {
    private final CommandBufferBackedList commandList = new CommandBufferBackedList();

    private final DeviceFunctions functions = new DeviceFunctions(this);

    private final List<VkObjectDestroyable>[] toDestroy = new List[3];
    private int frame;

    VKRenderDevice() {
        for (int i = 0; i < 3; i++) {
            this.toDestroy[i] = new ArrayList<>();
        }
    }

    public synchronized void flip() {
        frame = (frame + 1) % 3;
        var commandList = createCommandList();
        for (VkObjectDestroyable destroyable : toDestroy[frame]) {
            destroyable.destroy(commandList);
        }
        toDestroy[frame].clear();
    }

    @Override
    public CommandList createCommandList() {
        return this.commandList.using(Blaze3DAccess.getCleanCommandBuffer());
    }

    @Override
    public DeviceFunctions getDeviceFunctions() {
        return this.functions;
    }

    @Override
    public int getSubTexelPrecisionBits() {
        return Blaze3DAccess.getSubTexelBits();
    }

    @Override
    public synchronized void destroyObjectWhenSafe(VkObjectDestroyable destroyable) {
        if (destroyable != null) {
            this.toDestroy[frame].add(destroyable);
        }
    }

    @Override
    public VulkanRenderPass startRenderPass(VkCommandBuffer commandBuffer) {
        return new VulkanRenderPass(commandBuffer);
    }

    private static class CommandBufferBackedList implements CommandList {
        private VkCommandBuffer commandBuffer;
        private boolean isInvalid;
        private final VkFenceQueue fenceQueue = new VkFenceQueue();

        private CommandList using(VkCommandBuffer commandBuffer) {
            this.commandBuffer = commandBuffer;
            this.isInvalid = false;
            return this;
        }

        @Override
        public VkBuffer createBuffer(long bufferSize, VkMappingType mappingType, EnumBitField<VkBufferUsages> flags) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                LongBuffer pBuffer = stack.mallocLong(1);
                PointerBuffer pAllocation = stack.mallocPointer(1);

                VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack)
                        .sType$Default()
                        .size(bufferSize)
                        .usage(flags.getBitField());

                int theFlags;

                if (mappingType == VkMappingType.CPU_ONLY) {
                    theFlags = Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT | Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT;
                } else if (mappingType == VkMappingType.CPU_MAPPABLE) {
                    theFlags = Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT;
                } else {
                    theFlags = 0;
                }

                VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.calloc(stack)
                        .usage(mappingType == VkMappingType.CPU_ONLY ? Vma.VMA_MEMORY_USAGE_AUTO_PREFER_HOST : Vma.VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE)
                        .flags(theFlags);

                VmaAllocationInfo info = VmaAllocationInfo.calloc(stack);

                int res = Vma.vmaCreateBuffer(Blaze3DAccess.getAllocator(),
                        bufferCreateInfo,
                        allocationCreateInfo,
                        pBuffer,
                        pAllocation,
                        info);

                var buffer = new VkBuffer(pBuffer.get(0), pAllocation.get(0), bufferSize, null);

                if (mappingType == VkMappingType.CPU_ONLY) {
                    var mapping = new VkMapping(buffer, info.pMappedData(), bufferSize);
                    buffer.setMapping(mapping);
                }

                if (res != VK13.VK_SUCCESS) {
                    throw new RuntimeException("Failed to create buffer " + res);
                }

                return buffer;
            }
        }

        @Override
        public void copyBufferToBuffer(VkBuffer src, VkBuffer dst, long readOffset, long writeOffset, long bytes) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VK13.vkCmdCopyBuffer(commandBuffer, src.handle(), dst.handle(), VkBufferCopy.calloc(1, stack).size(bytes).srcOffset(readOffset).dstOffset(writeOffset));
            }
        }

        @Override
        public void deleteBuffer(VkBuffer buffer) {
            RenderDevice.INSTANCE.destroyObjectWhenSafe(buffer);
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() {
            this.isInvalid = true;
        }

        @Override
        public VkMapping mapBuffer(VkBuffer buffer, long offset, long length) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer pointerBuffer = stack.mallocPointer(1);
                int res = Vma.vmaMapMemory(Blaze3DAccess.getAllocator(), buffer.getAllocation(), pointerBuffer);
                if (res != VK13.VK_SUCCESS) {
                    throw new RuntimeException("Failed to map buffer memory " + res);
                }
                buffer.setMapping(new VkMapping(buffer, pointerBuffer.get(0), length));
                return buffer.getMapping();
            }
        }

        @Override
        public void unmap(VkMapping map) {
            Vma.vmaUnmapMemory(Blaze3DAccess.getAllocator(), map.getBuffer().getAllocation());
            map.getBuffer().setMapping(null);
        }

        @Override
        public void flushMappedRange(VkMapping map, int offset, int length) {
            Vma.vmaFlushAllocation(Blaze3DAccess.getAllocator(), map.getBuffer().getAllocation(), offset, length);
        }

        @Override
        public VkFence createFence() {
            return fenceQueue.take();
        }

        @Override
        public VulkanRenderPass startRenderPass(long colorTextureView) {
            return new VulkanRenderPass(commandBuffer, colorTextureView);
        }
    }
}
