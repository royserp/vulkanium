package net.rs.vulkanium.client.config.structure;

import net.rs.vulkanium.api.config.ConfigState;
import net.rs.vulkanium.api.config.option.FlagHook;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.function.BiConsumer;

public class FlagHookImpl implements FlagHook {
    private final BiConsumer<Collection<Identifier>, ConfigState> hook;
    private final Collection<Identifier> triggers;

    public FlagHookImpl(BiConsumer<Collection<Identifier>, ConfigState> hook, Collection<Identifier> triggers) {
        this.hook = hook;
        this.triggers = triggers;
    }

    @Override
    public Collection<Identifier> getTriggers() {
        return this.triggers;
    }

    @Override
    public void accept(Collection<Identifier> flags, ConfigState state) {
        this.hook.accept(flags, state);
    }
}
