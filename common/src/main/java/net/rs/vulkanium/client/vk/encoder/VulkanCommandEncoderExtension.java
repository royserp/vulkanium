package net.rs.vulkanium.client.vk.encoder;

import org.lwjgl.vulkan.VkCommandBuffer;

public interface VulkanCommandEncoderExtension {
    VkCommandBuffer vulkanium$getCommandBuffer();
}
