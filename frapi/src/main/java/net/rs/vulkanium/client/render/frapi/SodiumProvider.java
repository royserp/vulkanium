package net.rs.vulkanium.client.render.frapi;

import net.rs.vulkanium.client.services.FRAPIProvider;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;

public class VulkaniumProvider implements FRAPIProvider {
    @Override
    public void register() {
        Renderer.register(VulkaniumRenderer.INSTANCE);
    }
}
