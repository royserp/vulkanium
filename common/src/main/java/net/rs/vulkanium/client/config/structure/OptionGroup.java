package net.rs.vulkanium.client.config.structure;

import net.rs.vulkanium.client.config.search.SearchIndex;
import net.minecraft.network.chat.Component;

import java.util.List;

public record OptionGroup(Component name, List<Option> options) {
    public void registerTextSources(SearchIndex index, ModOptions modOptions, OptionPage page) {
        for (Option option : this.options) {
            option.registerTextSources(index, modOptions, page, this);
        }
    }
}
