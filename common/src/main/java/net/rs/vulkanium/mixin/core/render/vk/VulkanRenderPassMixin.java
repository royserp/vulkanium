package net.rs.vulkanium.mixin.core.render.vk;

import com.mojang.blaze3d.vulkan.VulkanRenderPass;
import net.rs.vulkanium.client.vk.renderpass.VulkanRenderPassExtension;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VulkanRenderPass.class)
public abstract class VulkanRenderPassMixin implements VulkanRenderPassExtension {
    @Shadow
    protected abstract VkCommandBuffer secondaryCommandBuffer();

    @Override
    public VkCommandBuffer vulkanium$getCommandBuffer() {
        return this.secondaryCommandBuffer();
    }
}
