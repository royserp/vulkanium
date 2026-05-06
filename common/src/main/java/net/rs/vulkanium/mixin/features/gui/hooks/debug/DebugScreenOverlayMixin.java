package net.rs.vulkanium.mixin.features.gui.hooks.debug;

import net.rs.vulkanium.client.render.VulkaniumWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Inject(method = "extractRenderState", at = @At(value = "RETURN"))
    private void injectAfterPop(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        if (Minecraft.getInstance().debugEntries.isOverlayVisible()) {
            VulkaniumWorldRenderer.instance().renderBufferDebug(guiGraphics);
        }
    }
}
