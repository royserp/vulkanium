package net.rs.vulkanium.mixin.core.render.vk.encoder;

import com.mojang.blaze3d.vulkan.VulkanCommandEncoder;
import net.rs.vulkanium.client.vk.encoder.VulkanCommandEncoderExtension;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = VulkanCommandEncoder.class, remap = false)
public abstract class VulkanCommandEncoderMixin implements VulkanCommandEncoderExtension {
    @Invoker("commandBuffer")
    protected abstract VkCommandBuffer invokeCommandBuffer();

    @Override
    public VkCommandBuffer vulkanium$getCommandBuffer() {
        return this.invokeCommandBuffer();
    }
}
