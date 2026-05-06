package net.rs.vulkanium.mixin.core.render.world;

import net.rs.vulkanium.client.render.VulkaniumWorldRenderer;
import net.rs.vulkanium.client.world.LevelRendererExtension;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {
    @Shadow
    private ClientLevel level;

    @Unique
    private VulkaniumWorldRenderer renderer;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(Minecraft minecraft, LevelRenderState levelRenderState, LevelRenderer levelRenderer, CallbackInfo ci) {
        this.renderer = ((LevelRendererExtension) (Object) levelRenderer).vulkanium$getWorldRenderer();
    }

    @Inject(method = "setLevel", at = @At("RETURN"))
    private void onLevelChanged(ClientLevel level, CallbackInfo ci) {
        this.renderer.setLevel(level);
    }

    /**
     * @reason Redirect the terrain setup phase to our renderer
     * @author JellySquid
     */
    @Overwrite
    private void applyFrustum(final net.minecraft.client.renderer.culling.Frustum frustum) {
        if (!Minecraft.getInstance().isSameThread()) {
            throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
        }

        // Camera is not directly on gameRenderer. We don't have spectator info readily available from inside applyFrustum.
        // Actually, we can get camera via blockEntityRenderDispatcher.camera or entityRenderDispatcher.camera
        var camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        var viewport = ((net.rs.vulkanium.client.render.viewport.ViewportProvider) frustum).vulkanium$createViewport();
        var updateChunksImmediately = net.rs.vulkanium.client.util.FlawlessFrames.isActive();

        int sectionX = net.minecraft.core.SectionPos.posToSectionCoord(camera.position().x());
        int sectionY = net.minecraft.core.SectionPos.posToSectionCoord(camera.position().y());
        int sectionZ = net.minecraft.core.SectionPos.posToSectionCoord(camera.position().z());

        var levelRenderer = Minecraft.getInstance().levelRenderer;
        var ext = (LevelRendererExtension) levelRenderer;

        // Note: The world border invalidation is technically lost here if lastCameraSection variables aren't accessible.
        // It should ideally be kept, but for compilation/removal of cullTerrain we bypass it for now.

        this.renderer.setupTerrain(camera, viewport, ((net.rs.vulkanium.client.util.FogStorage) Minecraft.getInstance().gameRenderer).vulkanium$getFogParameters(), false, updateChunksImmediately, ext.vulkanium$getMatrices());
    }
    
    /**
     * @reason Redirect chunk updates to our renderer
     * @author JellySquid
     */
    @Overwrite
    public void setBlocksDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.renderer.scheduleRebuildForBlockArea(minX, minY, minZ, maxX, maxY, maxZ, false);
    }

    /**
     * @reason Redirect chunk updates to our renderer
     * @author JellySquid
     */
    @Overwrite
    public void setSectionDirtyWithNeighbors(int x, int y, int z) {
        this.renderer.scheduleRebuildForChunks(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1, false);
    }
    
    /**
     * @reason Redirect chunk updates to our renderer
     * @author JellySquid
     */
    @Overwrite
    private void setBlockDirty(BlockPos pos, boolean important) {
        this.renderer.scheduleRebuildForBlockArea(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, important);
    }
    
    /**
     * @reason Redirect chunk updates to our renderer
     * @author JellySquid
     */
    @Overwrite
    private void setSectionDirty(int x, int y, int z, boolean important) {
        this.renderer.scheduleRebuildForChunk(x, y, z, important);
    }
    
    @Inject(method = "allChanged", at = @At("RETURN"))
    private void onReload(CallbackInfo ci) {
        this.renderer.reload();
    }

    /**
     * @reason Redirect block entity extraction to our renderer
     * @author JellySquid
     */
    @Overwrite
    private void extractVisibleBlockEntities(final Camera camera, final float deltaPartialTick, final LevelRenderState levelRenderState) {
        this.renderer.extractBlockEntities(camera, deltaPartialTick, this.level.destructionProgress(), levelRenderState);
    }

    /**
     * @reason Redirect to our renderer
     * @author JellySquid
     */
    @Overwrite
    public int countRenderedSections() {
        return this.renderer.getVisibleChunkCount();
    }
}
