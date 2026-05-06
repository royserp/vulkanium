package net.rs.vulkanium.client.gui.console;

import net.rs.vulkanium.client.console.Console;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ConsoleHooks {
    public static void extractRenderState(GuiGraphicsExtractor graphics, double currentTime) {
        ConsoleRenderer.INSTANCE.update(Console.INSTANCE, currentTime);
        ConsoleRenderer.INSTANCE.draw(graphics);
    }
}
