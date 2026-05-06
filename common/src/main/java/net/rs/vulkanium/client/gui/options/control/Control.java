package net.rs.vulkanium.client.gui.options.control;

import net.rs.vulkanium.client.config.structure.Option;
import net.rs.vulkanium.client.gui.ColorTheme;
import net.rs.vulkanium.client.util.Dim2i;
import net.minecraft.client.gui.screens.Screen;

public interface Control {
    Option getOption();

    ControlElement createElement(Screen screen, AbstractOptionList list, Dim2i dim, ColorTheme theme);

    int getMaxWidth();
}
