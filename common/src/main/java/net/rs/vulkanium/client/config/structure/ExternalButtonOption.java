package net.rs.vulkanium.client.config.structure;

import net.rs.vulkanium.client.config.value.DependentValue;
import net.rs.vulkanium.client.gui.options.control.Control;
import net.rs.vulkanium.client.gui.options.control.ExternalButtonControl;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.function.Consumer;

public class ExternalButtonOption extends StaticOption {
    final Consumer<Screen> currentScreenConsumer;

    public ExternalButtonOption(
            Identifier id,
            Collection<Identifier> dependencies,
            Component name,
            DependentValue<Boolean> enabled,
            Component tooltip,
            Consumer<Screen> currentScreenConsumer
    ) {
        super(id, dependencies, name, enabled, tooltip);
        this.currentScreenConsumer = currentScreenConsumer;
    }

    @Override
    Control createControl() {
        return new ExternalButtonControl(this, this.currentScreenConsumer);
    }

    public Consumer<Screen> getCurrentScreenConsumer() {
        return this.currentScreenConsumer;
    }
}
