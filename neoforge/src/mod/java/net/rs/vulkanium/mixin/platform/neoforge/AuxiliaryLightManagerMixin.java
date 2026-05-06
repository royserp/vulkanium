package net.rs.vulkanium.mixin.platform.neoforge;

import net.rs.vulkanium.client.world.VulkaniumAuxiliaryLightManager;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AuxiliaryLightManager.class)
public interface AuxiliaryLightManagerMixin extends VulkaniumAuxiliaryLightManager {
}
