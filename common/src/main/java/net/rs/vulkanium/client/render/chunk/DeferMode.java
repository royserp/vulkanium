package net.rs.vulkanium.client.render.chunk;

import net.rs.vulkanium.client.gui.options.TextProvider;
import net.minecraft.network.chat.Component;

public enum DeferMode implements TextProvider {
    ALWAYS("vulkanium.options.defer_chunk_updates.always", TaskQueueType.ALWAYS_DEFER),
    ONE_FRAME("vulkanium.options.defer_chunk_updates.one_frame", TaskQueueType.ONE_FRAME_DEFER),
    ZERO_FRAMES("vulkanium.options.defer_chunk_updates.zero_frames", TaskQueueType.ZERO_FRAME_DEFER);

    private final Component name;
    private final TaskQueueType importantRebuildQueueType;

    DeferMode(String name, TaskQueueType importantRebuildQueueType) {
        this.name = Component.translatable(name);
        this.importantRebuildQueueType = importantRebuildQueueType;
    }

    @Override
    public Component getLocalizedName() {
        return this.name;
    }

    public TaskQueueType getImportantRebuildQueueType() {
        return this.importantRebuildQueueType;
    }
}
