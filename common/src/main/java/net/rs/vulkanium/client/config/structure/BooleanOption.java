package net.rs.vulkanium.client.config.structure;

import net.rs.vulkanium.api.config.ConfigState;
import net.rs.vulkanium.api.config.StorageEventHandler;
import net.rs.vulkanium.api.config.option.OptionBinding;
import net.rs.vulkanium.api.config.option.OptionImpact;
import net.rs.vulkanium.client.config.value.DependentValue;
import net.rs.vulkanium.client.gui.options.control.Control;
import net.rs.vulkanium.client.gui.options.control.TickBoxControl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class BooleanOption extends StatefulOption<Boolean> {
    public BooleanOption(
            Identifier id,
            Collection<Identifier> dependencies,
            Component name,
            DependentValue<Boolean> enabled,
            StorageEventHandler storage,
            Function<Boolean, Component> tooltipProvider,
            OptionImpact impact,
            Set<Identifier> flags,
            DependentValue<Boolean> defaultValue,
            Boolean controlHiddenWhenDisabled,
            OptionBinding<Boolean> binding,
            Consumer<ConfigState> applyHook
    ) {
        super(id, dependencies, name, enabled, storage, tooltipProvider, impact, flags, defaultValue, controlHiddenWhenDisabled, binding, applyHook);
    }

    @Override
    Control createControl() {
        return new TickBoxControl(this);
    }

    @Override
    Boolean validateValue(Boolean value) {
        return value;
    }
}
