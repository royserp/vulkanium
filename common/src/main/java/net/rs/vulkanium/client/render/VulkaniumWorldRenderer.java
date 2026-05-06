package net.rs.vulkanium.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.rs.vulkanium.client.VulkaniumClientMod;
import net.rs.vulkanium.client.render.chunk.*;
import net.rs.vulkanium.client.render.chunk.lists.ChunkRenderList;
import net.rs.vulkanium.client.render.chunk.lists.SortedRenderLists;
import net.rs.vulkanium.client.render.chunk.map.ChunkTracker;
import net.rs.vulkanium.client.render.chunk.map.ChunkTrackerHolder;
import net.rs.vulkanium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.rs.vulkanium.client.render.chunk.translucent_sorting.SortBehavior;
import net.rs.vulkanium.client.render.viewport.Viewport;
import net.rs.vulkanium.client.services.PlatformRuntimeInformation;
import net.rs.vulkanium.client.util.FogParameters;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.rs.vulkanium.client.vk.device.CommandList;
import net.rs.vulkanium.client.vk.device.RenderDevice;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.profiling.Profiler;
import net.rs.vulkanium.mixin.core.MinecraftAccessor;
import org.joml.Vector3d;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.function.Consumer;

public class VulkaniumWorldRenderer {
    private final Minecraft client;

    private ClientLevel level;
    private RenderSectionManager renderSectionManager;

    private int renderDistance;
    private FogParameters lastFogParameters;

    public VulkaniumWorldRenderer(Minecraft client) {
        this.client = client;
    }

    public static VulkaniumWorldRenderer instance() {
        return ((net.rs.vulkanium.client.world.LevelRendererExtension) Minecraft.getInstance().levelRenderer).vulkanium$getWorldRenderer();
    }

    public static VulkaniumWorldRenderer instanceNullable() {
        var renderer = Minecraft.getInstance().levelRenderer;
        if (renderer == null) {
            return null;
        }

        return ((net.rs.vulkanium.client.world.LevelRendererExtension) renderer).vulkanium$getWorldRenderer();
    }

    public void setLevel(ClientLevel level) {
        this.level = level;

        if (this.renderSectionManager != null) {
            this.renderSectionManager.destroy();
            this.renderSectionManager = null;
        }

        if (level != null) {
            this.loadLevel(level);
        }
    }

    private void loadLevel(ClientLevel level) {
        try (CommandList commandList = RenderDevice.INSTANCE.createCommandList()) {
            this.initRenderer(commandList);
        }
    }

    public void onExtract(Camera camera, Viewport viewport, FogParameters fogParameters, boolean updateChunksImmediately) {
        this.lastFogParameters = fogParameters;

        this.renderSectionManager.prepareFrame(new Vector3d(camera.position().x, camera.position().y, camera.position().z));

        this.processChunkEvents();

        if (this.renderDistance != this.client.options.getEffectiveRenderDistance()) {
            this.reload();
        }

        if (viewport != null) {
            this.renderSectionManager.update(camera, viewport, fogParameters, this.client.player != null && this.client.player.isSpectator());
            this.renderSectionManager.finalizeRenderLists(viewport);
        }

        this.renderSectionManager.updateChunks(updateChunksImmediately);
        this.renderSectionManager.uploadChunks();

        var profiler = Profiler.get();
        profiler.push("chunk_render_tick");

        this.renderSectionManager.tickVisibleRenders();

        profiler.pop();

        Entity.setViewScale(Mth.clamp((double) this.client.options.getEffectiveRenderDistance() / 8.0D, 1.0D, 2.5D) * this.client.options.entityDistanceScaling().get());
    }

    public void updateViewport(Viewport viewport) {
        this.renderSectionManager.finalizeRenderLists(viewport);
    }

    private void processChunkEvents() {
        this.renderSectionManager.beforeSectionUpdates();
        var tracker = ChunkTrackerHolder.get(this.level);
        tracker.forEachEvent(this.renderSectionManager::onChunkAdded, this.renderSectionManager::onChunkRemoved);
    }

    /**
     * Performs a render pass for the given {@link net.minecraft.client.renderer.RenderType} and draws all visible chunks for it.
     */
    public void drawChunkLayer(ChunkSectionLayerGroup group, ChunkRenderMatrices matrices, double x, double y, double z, GpuSampler terrainSampler, VkCommandBuffer commandBuffer) {
        if (group == ChunkSectionLayerGroup.OPAQUE) {
            this.renderSectionManager.renderLayer(matrices, DefaultTerrainRenderPasses.SOLID, x, y, z, this.lastFogParameters, terrainSampler, commandBuffer);
            this.renderSectionManager.renderLayer(matrices, DefaultTerrainRenderPasses.CUTOUT, x, y, z, this.lastFogParameters, terrainSampler, commandBuffer);
        } else if (group == ChunkSectionLayerGroup.TRANSLUCENT) {
            this.renderSectionManager.renderLayer(matrices, DefaultTerrainRenderPasses.TRANSLUCENT, x, y, z, this.lastFogParameters, terrainSampler, commandBuffer);
        }
    }

    public void reload() {
        if (this.level == null) {
            return;
        }

        try (CommandList commandList = RenderDevice.INSTANCE.createCommandList()) {
            this.initRenderer(commandList);
        }
    }

    private void initRenderer(CommandList commandList) {
        if (this.renderSectionManager != null) {
            this.renderSectionManager.destroy();
            this.renderSectionManager = null;
        }

        // translucency sorting can be disabled in development environments by setting the debug option in the config file
        var sortBehavior = SortBehavior.DYNAMIC_DEFER_NEARBY_ZERO_FRAMES;

        if (PlatformRuntimeInformation.getInstance().isDevelopmentEnvironment()
                && !VulkaniumClientMod.options().debug.terrainSortingEnabled) {
            sortBehavior = SortBehavior.OFF;
        }

        this.renderDistance = this.client.options.getEffectiveRenderDistance();

        this.renderSectionManager = new RenderSectionManager(this.level, this.renderDistance, sortBehavior, commandList);

        var tracker = ChunkTrackerHolder.get(this.level);
        ChunkTracker.forEachChunk(tracker.getReadyChunks(), this.renderSectionManager::onChunkAdded);
    }

    public void extractBlockEntities(Camera camera, float tickDelta, Long2ObjectMap<SortedSet<BlockDestructionProgress>> progression, LevelRenderState levelRenderState) {
        PoseStack stack = new PoseStack();

        SortedRenderLists renderLists = this.renderSectionManager.getRenderLists();
        Iterator<ChunkRenderList> renderListIterator = renderLists.iterator();

        while (renderListIterator.hasNext()) {
            var renderList = renderListIterator.next();

            var region = renderList.getRegion();
            var iterator = renderList.sectionsWithEntitiesIterator();

            if (iterator == null) {
                continue;
            }

            while (iterator.hasNext()) {
                var section = region.getSection(iterator.nextByteAsInt());

                if (section == null) {
                    continue;
                }

                this.forEachBlockEntity(section, blockEntity -> {
                    var pos = blockEntity.getBlockPos();
                    var progresses = progression.get(pos.asLong());

                    ModelFeatureRenderer.CrumblingOverlay breakProgress;
                    if (progresses != null && !progresses.isEmpty()) {
                        stack.pushPose();
                        stack.translate(pos.getX() - camera.position().x, pos.getY() - camera.position().y, pos.getZ() - camera.position().z);
                        breakProgress = new ModelFeatureRenderer.CrumblingOverlay(progresses.last().getProgress(), stack.last());
                        stack.popPose();
                    } else {
                        breakProgress = null;
                    }

                    var state = ((MinecraftAccessor) this.client).getBlockEntityRenderDispatcher().tryExtractRenderState(blockEntity, tickDelta, breakProgress, false);
                    if (state != null) {
                        levelRenderState.blockEntityRenderStates.add(state);
                    }
                });
            }
        }
    }

    private void forEachBlockEntity(RenderSection section, Consumer<net.minecraft.world.level.block.entity.BlockEntity> consumer) {
        var culled = section.getCulledBlockEntities();
        if (culled != null) {
            for (var be : culled) {
                consumer.accept(be);
            }
        }

        var global = section.getGlobalBlockEntities();
        if (global != null) {
            for (var be : global) {
                consumer.accept(be);
            }
        }
    }

    public void iterateVisibleBlockEntities(java.util.function.Consumer<net.minecraft.world.level.block.entity.BlockEntity> blockEntityConsumer) {
        SortedRenderLists renderLists = this.renderSectionManager.getRenderLists();
        Iterator<ChunkRenderList> renderListIterator = renderLists.iterator();

        while (renderListIterator.hasNext()) {
            var renderList = renderListIterator.next();

            var region = renderList.getRegion();
            var iterator = renderList.sectionsWithEntitiesIterator();

            if (iterator == null) {
                continue;
            }

            while (iterator.hasNext()) {
                var section = region.getSection(iterator.nextByteAsInt());

                if (section == null) {
                    continue;
                }

                this.forEachBlockEntity(section, blockEntityConsumer);
            }
        }
    }

    public Collection<String> getDebugStrings(boolean verbose) {
        return this.renderSectionManager.getDebugStrings(verbose);
    }

    public int getVisibleChunkCount() {
        return this.renderSectionManager.getVisibleChunkCount();
    }

    public boolean isTerrainRenderComplete() {
        return this.renderSectionManager.isTerrainRenderComplete();
    }

    public void scheduleTerrainUpdate() {
        this.renderSectionManager.markGraphDirty();
    }

    public void scheduleRebuildForChunk(int x, int y, int z, boolean important) {
        this.renderSectionManager.scheduleRebuildForChunk(x, y, z, important);
    }

    public void scheduleRebuildForBlockArea(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
        this.renderSectionManager.scheduleRebuildForBlockArea(minX, minY, minZ, maxX, maxY, maxZ, important);
    }

    public void scheduleRebuildForChunks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
        this.renderSectionManager.scheduleRebuildForChunks(minX, minY, minZ, maxX, maxY, maxZ, important);
    }

    public boolean isSectionReady(int x, int y, int z) {
        return this.renderSectionManager.isSectionBuilt(x, y, z);
    }

    public void renderBufferDebug(GuiGraphicsExtractor guiGraphics) {
        if (this.renderSectionManager != null) {
            this.renderSectionManager.renderBufferDebug(guiGraphics);
        }
    }

    public boolean isEntityVisible(net.minecraft.client.renderer.entity.EntityRenderer<?, ?> renderer, Entity entity) {
        return true; // Stub
    }
}
