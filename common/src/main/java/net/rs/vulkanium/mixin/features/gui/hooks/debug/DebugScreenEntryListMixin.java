package net.rs.vulkanium.mixin.features.gui.hooks.debug;

import net.rs.vulkanium.client.VulkaniumClientMod;
import net.rs.vulkanium.client.services.PlatformRuntimeInformation;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import org.objectweb.asm.Opcodes;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(DebugScreenEntryList.class)
public class DebugScreenEntryListMixin {
    @Shadow
    private Map<Identifier, DebugScreenEntryStatus> allStatuses;
    
    @Unique
    private void setFullDebugStatuses() {
        this.allStatuses.put(DebugScreenEntries.CHUNK_RENDER_STATS, DebugScreenEntryStatus.IN_OVERLAY);
        this.allStatuses.put(DebugScreenEntries.ENTITY_RENDER_STATS, DebugScreenEntryStatus.IN_OVERLAY);
        this.allStatuses.put(DebugScreenEntries.PARTICLE_RENDER_STATS, DebugScreenEntryStatus.IN_OVERLAY);
        this.allStatuses.put(DebugScreenEntries.MEMORY, DebugScreenEntryStatus.IN_OVERLAY);
        this.allStatuses.put(DebugScreenEntries.SYSTEM_SPECS, DebugScreenEntryStatus.IN_OVERLAY);
    }

    @Unique
    private void setReducedDebugStatuses() {
        this.allStatuses.put(DebugScreenEntries.CHUNK_RENDER_STATS, DebugScreenEntryStatus.IN_OVERLAY);
    }

    @Inject(method = "resetToProfile", at = @At(value = "RETURN"))
    private void injectLoadProfile(DebugScreenProfile debugScreenProfile, CallbackInfo ci) {
        if (debugScreenProfile == DebugScreenProfile.PERFORMANCE && !PlatformRuntimeInformation.getInstance().isDevelopmentEnvironment()) {
            this.setReducedDebugStatuses();
        } else {
            this.setFullDebugStatuses();
        }
    }

    @Inject(method = "rebuildCurrentList", at = @At("HEAD"))
    private void injectVulkaniumSettings(CallbackInfo ci) {
        Identifier setting = PlatformRuntimeInformation.getInstance().isDevelopmentEnvironment() ? VulkaniumClientMod.SODIUM_DEBUG_ENTRY_FULL : VulkaniumClientMod.SODIUM_DEBUG_ENTRY_REDUCED;
        if (!this.allStatuses.containsKey(setting)) {
            this.allStatuses.put(setting, DebugScreenEntryStatus.IN_OVERLAY);
        }
    }
}

