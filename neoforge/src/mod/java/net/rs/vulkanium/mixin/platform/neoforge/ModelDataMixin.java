package net.rs.vulkanium.mixin.platform.neoforge;

import net.rs.vulkanium.client.services.VulkaniumModelData;
import net.neoforged.neoforge.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelData.class)
public class ModelDataMixin implements VulkaniumModelData {
}
