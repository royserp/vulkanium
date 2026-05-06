package net.rs.vulkanium.client.gui;

import net.rs.vulkanium.client.VulkaniumClientMod;
import net.rs.vulkanium.client.render.VulkaniumWorldRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

public class VulkaniumDebugEntry implements DebugScreenEntry {
    private static final Identifier DEBUG_GROUP = Identifier.fromNamespaceAndPath("vulkanium", "debug_group");
    private final boolean verbose;

    public VulkaniumDebugEntry(boolean verbose) {
        this.verbose = verbose;
    }

    private static ChatFormatting getVersionColor() {
        String version = VulkaniumClientMod.getVersion();
        ChatFormatting color;

        if (version.contains("-local")) {
            color = ChatFormatting.RED;
        } else if (version.contains("-snapshot")) {
            color = ChatFormatting.LIGHT_PURPLE;
        } else {
            color = ChatFormatting.GREEN;
        }

        return color;
    }

    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
        debugScreenDisplayer.addToGroup(DEBUG_GROUP, "%sVulkanium Renderer (%s)".formatted(getVersionColor(), VulkaniumClientMod.getVersion()));

        var renderer = VulkaniumWorldRenderer.instanceNullable();

        if (renderer != null) {
            debugScreenDisplayer.addToGroup(DEBUG_GROUP, renderer.getDebugStrings(this.verbose));
        }
    }
}
