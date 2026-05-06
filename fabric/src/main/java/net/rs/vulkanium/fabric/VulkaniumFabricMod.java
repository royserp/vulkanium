package net.rs.vulkanium.fabric;

import net.rs.vulkanium.client.VulkaniumClientMod;
import net.rs.vulkanium.client.services.FRAPIProvider;
import net.rs.vulkanium.client.util.FlawlessFrames;
import net.rs.vulkanium.fabric.config.ConfigLoaderFabric;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import java.util.function.Consumer;

public class VulkaniumFabricMod implements ClientModInitializer {
    @Override
    @SuppressWarnings("unchecked")
    public void onInitializeClient() {
        ModContainer mod = FabricLoader.getInstance()
                .getModContainer("vulkanium")
                .orElseThrow(NullPointerException::new);

        ConfigLoaderFabric.collectConfigEntryPoints();
        VulkaniumClientMod.onInitialization(mod.getMetadata().getVersion().getFriendlyString());

        FabricLoader.getInstance()
                .getEntrypoints("frex_flawless_frames", Consumer.class)
                .forEach(api -> api.accept(FlawlessFrames.getProvider()));

        FRAPIProvider.getInstance().register();
    }
}
