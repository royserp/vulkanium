package net.rs.vulkanium.mixin.core.render.world;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import net.rs.vulkanium.client.render.VulkaniumWorldRenderer;
import net.rs.vulkanium.client.render.chunk.ChunkRenderMatrices;
import net.rs.vulkanium.client.util.GameRendererStorage;
import net.rs.vulkanium.client.util.VulkaniumChunkSection;
import net.rs.vulkanium.client.world.LevelRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.EnumMap;
import java.util.List;

@Mixin(LevelRenderer.class)
public abstract class OverworldRendererMixin implements LevelRendererExtension {
    @Unique
    private static final EnumMap<ChunkSectionLayer, Int2ObjectOpenHashMap<List<RenderPass.Draw<GpuBufferSlice[]>>>> STATIC_MAP = new EnumMap<>(ChunkSectionLayer.class);

    @Unique
    private VulkaniumWorldRenderer renderer;

    @Unique
    private ChunkRenderMatrices matrices;

    @Override
    public VulkaniumWorldRenderer vulkanium$getWorldRenderer() {
        return this.renderer;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, ModelManager modelManager, TextureManager textureManager, AtlasManager atlasManager, ShaderManager shaderManager, GameRenderer gameRenderer, int i, int j, CallbackInfo ci) {
        this.renderer = new VulkaniumWorldRenderer(Minecraft.getInstance());
    }

    /**
     * @reason Redirect to our renderer
     * @author JellySquid
     */
    @Overwrite
    public ChunkSectionsToRender prepareChunkRenders(Matrix4fc modelViewMatrix) {
        var minecraft = Minecraft.getInstance();
        this.matrices = new ChunkRenderMatrices(((GameRendererStorage) minecraft.gameRenderer).vulkanium$getProjectionMatrix(), modelViewMatrix);
        
        ChunkSectionsToRender chunkSectionsToRender = new ChunkSectionsToRender(minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView(), STATIC_MAP, -1, new GpuBufferSlice[0]);
        
        // We use the last camera position for culling/sorting
        var camera = minecraft.getEntityRenderDispatcher().camera;
        ((VulkaniumChunkSection) (Object) chunkSectionsToRender).vulkanium$setRendering(renderer, matrices, camera.position().x(), camera.position().y(), camera.position().z());
        
        return chunkSectionsToRender;
    }

    @Override
    public void vulkanium$setMatrices(ChunkRenderMatrices matrices) {
        this.matrices = matrices;
    }

    @Override
    public ChunkRenderMatrices vulkanium$getMatrices() {
        return this.matrices;
    }
}
