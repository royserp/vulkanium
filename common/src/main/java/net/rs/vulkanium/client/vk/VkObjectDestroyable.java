package net.rs.vulkanium.client.vk;

import net.rs.vulkanium.client.vk.device.CommandList;

public abstract class VkObjectDestroyable extends VkObject {
    protected abstract void destroyInternal(CommandList commandList);

    public void destroy(CommandList commandList) {
        this.destroyInternal(commandList);
        this.invalidateHandle();
    }
}
