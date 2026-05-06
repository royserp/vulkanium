package net.rs.vulkanium.client.config.structure;

import net.minecraft.resources.Identifier;

public record OptionOverride(Identifier target, String source, Option change) {
}
