/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.rs.vulkanium.client.render.frapi;

import com.mojang.blaze3d.vertex.PoseStack;
import net.rs.vulkanium.client.render.frapi.mesh.MutableMeshImpl;
import net.rs.vulkanium.client.render.frapi.render.AccessLayerRenderState;
import net.rs.vulkanium.client.render.frapi.render.NonTerrainBlockRenderContext;
import net.rs.vulkanium.client.render.frapi.render.SimpleBlockRenderContext;
import net.rs.vulkanium.client.render.frapi.wrapper.ExtendedMutableQuadViewImpl;
import net.rs.vulkanium.client.render.model.MutableQuadViewImpl;
import net.rs.vulkanium.client.services.FRAPIProvider;
import net.rs.vulkanium.mixin.frapi.BlockRenderDispatcherAccessor;
import net.rs.vulkanium.mixin.frapi.ModelBlockRendererAccessor;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.BlockMultiBufferSource;
import net.fabricmc.fabric.api.client.renderer.v1.render.ChunkSectionLayerHelper;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricModelBlockRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.ItemRenderTypeGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * The Vulkanium renderer implementation.
 */
public class VulkaniumRenderer implements Renderer {
    public static final VulkaniumRenderer INSTANCE = new VulkaniumRenderer();
    public static final Consumer<MutableQuadViewImpl> BUFFERER = quad -> ((ExtendedMutableQuadViewImpl) quad).getWrapper().transformAndEmit();

    private VulkaniumRenderer() { }

    @Override
    public MutableMesh mutableMesh() {
        return new MutableMeshImpl();
    }


    @Override
    public void render(ModelBlockRenderer modelBlockRenderer, BlockAndTintGetter blockView, BlockStateModel model, BlockState state, BlockPos pos, PoseStack poseStack, BlockMultiBufferSource multiBufferSource, boolean cull, long seed, int overlay) {
        NonTerrainBlockRenderContext.POOL.get().renderModel(blockView, ((ModelBlockRendererAccessor) modelBlockRenderer).getBlockColors(), model, state, pos, poseStack, multiBufferSource, cull, seed, overlay);
    }

    @Override
    public void render(PoseStack.Pose entry, BlockMultiBufferSource vertexConsumers, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
        SimpleBlockRenderContext.POOL.get().bufferModel(entry, vertexConsumers, model, red, green, blue, light, overlay, blockView, pos, state);
    }

    @Override
    public void renderBlockAsEntity(BlockRenderDispatcher renderManager, BlockState state, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay, BlockAndTintGetter blockView, BlockPos pos) {
        RenderShape renderShape = state.getRenderShape();

        if (renderShape != RenderShape.INVISIBLE) {
            BlockStateModel model = renderManager.getBlockModel(state);
            int tint = ((ModelBlockRendererAccessor) renderManager.getModelRenderer()).getBlockColors().getColor(state, null, null, 0);
            float red = (tint >> 16 & 255) / 255.0F;
            float green = (tint >> 8 & 255) / 255.0F;
            float blue = (tint & 255) / 255.0F;

            FabricModelBlockRenderer.render(poseStack.last(), ChunkSectionLayerHelper.entityDelegate(multiBufferSource), model, red, green, blue, light, overlay, blockView, pos, state);
        }
    }

    @Override
    public void setLayerRenderTypeGetter(
            ItemStackRenderState.LayerRenderState layer,
            ItemRenderTypeGetter renderTypeGetter
    ) {
        ((AccessLayerRenderState) layer).fabric_setRenderTypeGetter(renderTypeGetter);
    }

    @Override
    public QuadEmitter getLayerRenderStateEmitter(ItemStackRenderState.LayerRenderState layer) {
        return ((AccessLayerRenderState) layer).fabric_getMutableMesh().emitter();
    }
}
