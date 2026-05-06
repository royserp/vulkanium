package net.rs.vulkanium.client.vk.renderpass;

import org.lwjgl.vulkan.VkCommandBuffer;

public interface VulkanRenderPassExtension {
    VkCommandBuffer vulkanium$getCommandBuffer();
}
