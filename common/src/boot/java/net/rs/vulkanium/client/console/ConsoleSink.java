package net.rs.vulkanium.client.console;

import net.rs.vulkanium.client.console.message.MessageLevel;
import org.jspecify.annotations.NonNull;

public interface ConsoleSink {
    void logMessage(@NonNull MessageLevel level, @NonNull String text, boolean translatable, double duration);
}
