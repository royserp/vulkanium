package net.rs.vulkanium.api.config.option;

import net.rs.vulkanium.api.config.ConfigState;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * A hook that is triggered when certain option flags are updated.
 */
public interface FlagHook extends BiConsumer<Collection<Identifier>, ConfigState> {
    /**
     * Gets the identifiers of the flags that trigger this hook.
     *
     * @return A collection of flag identifiers.
     */
    Collection<Identifier> getTriggers();
}
