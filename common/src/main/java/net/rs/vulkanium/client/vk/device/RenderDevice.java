package net.rs.vulkanium.client.vk.device;

import net.rs.vulkanium.client.vk.VkObjectDestroyable;
import net.rs.vulkanium.client.vk.functions.DeviceFunctions;
import net.rs.vulkanium.client.vk.renderpass.VulkanRenderPass;
import org.lwjgl.vulkan.VkCommandBuffer;

public interface RenderDevice {
    RenderDevice INSTANCE = new VKRenderDevice();

    CommandList createCommandList();

    DeviceFunctions getDeviceFunctions();

    int getSubTexelPrecisionBits();

    void destroyObjectWhenSafe(VkObjectDestroyable destroyable);

    void flip();

    VulkanRenderPass startRenderPass(VkCommandBuffer commandBuffer);
}
