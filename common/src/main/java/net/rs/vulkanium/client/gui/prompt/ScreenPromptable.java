package net.rs.vulkanium.client.gui.prompt;

import net.rs.vulkanium.client.gui.Dimensioned;
import org.jspecify.annotations.Nullable;

public interface ScreenPromptable extends Dimensioned {
    void setPrompt(@Nullable ScreenPrompt prompt);

    @Nullable ScreenPrompt getPrompt();
}
