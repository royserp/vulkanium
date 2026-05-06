package net.rs.vulkanium.client.vk.pipeline;

import net.rs.vulkanium.client.vk.Blaze3DAccess;
import net.rs.vulkanium.client.vk.VkObjectDestroyable;
import net.rs.vulkanium.client.vk.device.CommandList;
import org.lwjgl.vulkan.VK13;

public final class VkPipelineLayout extends VkObjectDestroyable {
    public VkPipelineLayout(long handle) {
        this.setHandle(handle);
    }

    public void delete() {
        this.destroyInternal(null);
        this.invalidateHandle();
    }

    @Override
    protected void destroyInternal(CommandList commandList) {
        VK13.vkDestroyPipelineLayout(Blaze3DAccess.getDevice(), this.handle(), null);
    }
}
