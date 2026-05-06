package net.rs.vulkanium.client.config.structure;

import net.rs.vulkanium.client.config.builder.OptionBuilderImpl;
import net.minecraft.resources.Identifier;

public record OptionOverlay(Identifier target, String source, OptionBuilderImpl<?> change) {
}
