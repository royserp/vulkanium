package net.rs.vulkanium.mixin.core.render.vk;

import com.mojang.blaze3d.vulkan.VulkanRenderPass;
import net.rs.vulkanium.client.vk.renderpass.VulkanRenderPassExtension;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = VulkanRenderPass.class, remap = false)
public abstract class VulkanRenderPassMixin implements VulkanRenderPassExtension {
    @Invoker("secondaryCommandBuffer")
    protected abstract VkCommandBuffer invokeSecondaryCommandBuffer();

    @Override
    public VkCommandBuffer vulkanium$getCommandBuffer() {
        return this.invokeSecondaryCommandBuffer();
    }
}
