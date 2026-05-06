package net.rs.vulkanium.neoforge;

import net.rs.vulkanium.client.gui.VideoSettingsScreen;
import net.rs.vulkanium.client.services.FRAPIProvider;
import net.rs.vulkanium.client.util.FlawlessFrames;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforgespi.language.IModInfo;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

@Mod(value = "vulkanium", dist = Dist.CLIENT)
public class VulkaniumForgeMod {
    public VulkaniumForgeMod(IEventBus bus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, screen) -> VideoSettingsScreen.createScreen(screen));

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        for (IModInfo mod : ModList.get().getMods()) {
            String handler = (String) mod.getModProperties().getOrDefault("frex:flawless_frames_handler", null);

            if (handler == null) {
                continue;
            }

            try {
                lookup.findStatic(Class.forName(handler), "acceptController", MethodType.methodType(void.class, Function.class)).invoke(FlawlessFrames.getProvider());
            } catch (Throwable e) {
                throw new RuntimeException("Failed to execute Flawless Frames handler for mod " + mod.getModId() + "!", e);
            }
        }

        FRAPIProvider.getInstance().register();
    }
}