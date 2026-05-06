package net.rs.vulkanium.client.config.structure;

import com.google.common.collect.ImmutableList;
import net.rs.vulkanium.client.config.search.SearchIndex;
import net.minecraft.network.chat.Component;

public record OptionPage(Component name, ImmutableList<OptionGroup> groups) implements Page {
    @Override
    public void registerTextSources(SearchIndex index, ModOptions modOptions) {
        for (OptionGroup group : this.groups()) {
            group.registerTextSources(index, modOptions, this);
        }
    }
}
