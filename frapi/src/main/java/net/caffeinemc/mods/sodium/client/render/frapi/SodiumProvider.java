package net.caffeinemc.mods.sodium.client.render.frapi;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.RendererProvider;

public class SodiumProvider implements RendererProvider {
    @Override
    public Renderer getRenderer() {
        return SodiumRenderer.INSTANCE;
    }
}
