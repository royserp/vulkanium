package net.rs.vulkanium.client.config.structure;

import com.google.common.collect.ImmutableList;
import net.rs.vulkanium.api.config.option.FlagHook;
import net.rs.vulkanium.client.config.search.SearchIndex;
import net.rs.vulkanium.client.config.search.Searchable;
import net.rs.vulkanium.client.gui.ColorTheme;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;

public record ModOptions(String configId, String name, String version, ColorTheme theme, Identifier icon, boolean iconMonochrome, ImmutableList<Page> pages, List<OptionOverride> overrides, List<OptionOverlay> overlays, Collection<FlagHook> flagHooks) implements Searchable {
    @Override
    public void registerTextSources(SearchIndex index) {
        for (Page page : this.pages) {
            page.registerTextSources(index, this);
        }
    }
}
