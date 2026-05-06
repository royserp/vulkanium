package net.rs.vulkanium.client.vk;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vulkan.VulkanCommandEncoder;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuSampler;
import com.mojang.blaze3d.vulkan.VulkanGpuTextureView;
import net.rs.vulkanium.mixin.core.GpuDeviceAccessor;
import org.jspecify.annotations.Nullable;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;

public class Blaze3DAccess {
    private static VkDevice device;
    private static long allocator;

    public static VkCommandBuffer getCleanCommandBuffer() {
        VulkanCommandEncoder encoder = getVulkanDevice().createCommandEncoder();
        return encoder.allocateTransientCommandBuffer(true);
    }

    public static int getSubTexelBits() {
        return 4;
    }

    public static VkDevice getDevice() {
        if (device != null) {
            return device;
        }
        device = getVulkanDevice().vkDevice();
        return device;
    }

    public static long getAllocator() {
        if (allocator != 0) {
            return allocator;
        }
        allocator = getVulkanDevice().vma();
        return allocator;
    }

    public static long getView(@Nullable GpuTextureView colorTextureView) {
        if (colorTextureView == null) {
            return 0L;
        }
        return ((VulkanGpuTextureView) colorTextureView).vkImageView();
    }

    public static void registerExtensions() {
        // Vanilla Vulkan backend handles feature and extension setup internally.
    }

    public static long getSampler(GpuSampler sampler) {
        return ((VulkanGpuSampler) sampler).vkSampler();
    }

    private static VulkanDevice getVulkanDevice() {
        return (VulkanDevice) ((GpuDeviceAccessor) RenderSystem.getDevice()).getBackend();
    }
}
