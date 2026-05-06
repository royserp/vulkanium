package net.rs.vulkanium.neoforge.model;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.rs.vulkanium.client.render.helper.ListStorage;
import net.rs.vulkanium.client.render.model.AbstractBlockRenderContext;
import net.rs.vulkanium.client.services.PlatformModelAccess;
import net.rs.vulkanium.client.services.VulkaniumModelData;
import net.rs.vulkanium.client.services.VulkaniumModelDataContainer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NeoForgeModelAccess implements PlatformModelAccess {
    @Override
    public List<BakedQuad> getQuads(BlockAndTintGetter level, BlockPos pos, BlockStateModelPart model, BlockState state, Direction face, RandomSource random, ChunkSectionLayer renderType) {
        return model.getQuads(face);
    }

    @Override
    public VulkaniumModelDataContainer getModelDataContainer(Level level, SectionPos sectionPos) {
        Set<Long2ObjectMap.Entry<ModelData>> entrySet = level.getModelDataManager().getAt(sectionPos).long2ObjectEntrySet();
        Long2ObjectMap<VulkaniumModelData> modelDataMap = new Long2ObjectOpenHashMap<>(entrySet.size());

        for (Long2ObjectMap.Entry<ModelData> entry : entrySet) {
            modelDataMap.put(entry.getLongKey(), (VulkaniumModelData) (Object) entry.getValue());
        }

        return new VulkaniumModelDataContainer(modelDataMap);
    }

    @Override
    public List<BlockStateModelPart> collectPartsOf(BlockStateModel blockStateModel, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, ListStorage emitter) {
        List<BlockStateModelPart> parts = emitter == null ? new ArrayList<>() : emitter.clearAndGet();
        blockStateModel.collectParts(blockView, pos, state, random, parts);
        return parts;
    }

    @Override
    public VulkaniumModelData getEmptyModelData() {
        return (VulkaniumModelData) (Object) ModelData.EMPTY;
    }

    @Override
    public ChunkSectionLayer getPartRenderType(BlockStateModelPart part, BlockState state, ChunkSectionLayer defaultType) {
        return part.getRenderType(state);
    }
}
