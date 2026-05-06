package net.rs.vulkanium.mixin.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import net.rs.vulkanium.client.VulkaniumClientMod;
import net.rs.vulkanium.client.checks.ResourcePackScanner;
import net.rs.vulkanium.client.config.ConfigManager;
import net.rs.vulkanium.client.gui.VulkaniumConfigBuilder;
import net.rs.vulkanium.client.vk.Blaze3DAccess;
import net.minecraft.client.GameLoadCookie;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    /**
     * Check for problematic core shader resource packs after the initial game launch.
     */
    @Inject(method = "onGameLoadFinished", at = @At("TAIL"))
    private void postInit(GameLoadCookie cookie, CallbackInfo ci) {
        ResourcePackScanner.checkIfCoreShaderLoaded(this.resourceManager);

        ConfigManager.registerConfigsLate();
    }

    /**
     * Check for problematic core shader resource packs after every resource reload.
     */
    @Inject(method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;", at = @At("TAIL"))
    private void postResourceReload(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ResourcePackScanner.checkIfCoreShaderLoaded(this.resourceManager);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;registerTextures(Lnet/minecraft/client/renderer/texture/TextureManager;)V"))
    private void registerVulkaniumIcon(TextureManager textureManager, Operation<Void> original) {
        VulkaniumConfigBuilder.registerIcon(textureManager);
        original.call(textureManager);
    }

    @Inject(method = "<init>", at = @At("HEAD"))
    private static void registerExtensions(GameConfig gameConfig, CallbackInfo ci) {
        Blaze3DAccess.registerExtensions();
    }
}
