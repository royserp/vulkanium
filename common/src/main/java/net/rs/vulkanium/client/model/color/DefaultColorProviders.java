package net.rs.vulkanium.client.model.color;

import net.rs.vulkanium.client.model.quad.ModelQuadView;
import net.rs.vulkanium.client.model.quad.blender.BlendedColorProvider;
import net.rs.vulkanium.client.world.LevelSlice;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;

public class DefaultColorProviders {
    public static ColorProvider<BlockState> adapt(List<BlockTintSource> color) {
        return new VanillaAdapter(color);
    }

    public static class GrassColorProvider<T> extends BlendedColorProvider<T> {
        public static final ColorProvider<BlockState> BLOCKS = new GrassColorProvider<>();

        private GrassColorProvider() {

        }

        @Override
        protected int getColor(LevelSlice slice, T state, BlockPos pos) {
            return 0xFF000000 | BiomeColors.getAverageGrassColor(slice, pos);
        }
    }

    public static class FoliageColorProvider<T> extends BlendedColorProvider<T> {
        public static final ColorProvider<BlockState> BLOCKS = new FoliageColorProvider<>();

        private FoliageColorProvider() {

        }

        @Override
        protected int getColor(LevelSlice slice, T state, BlockPos pos) {
            return 0xFF000000 | BiomeColors.getAverageFoliageColor(slice, pos);
        }
    }

    private static class VanillaAdapter implements ColorProvider<BlockState> {
        private final List<BlockTintSource> sources;

        private VanillaAdapter(List<BlockTintSource> sources) {
            this.sources = sources;
        }

        @Override
        public void getColors(LevelSlice slice, BlockPos pos, BlockPos.MutableBlockPos scratchPos, BlockState state, ModelQuadView quad, int[] output, boolean smooth) {
            int tintIndex = quad.getTintIndex();
            int color = 0xFFFFFFFF;

            if (tintIndex >= 0 && tintIndex < this.sources.size()) {
                color = 0xFF000000 | this.sources.get(tintIndex).colorInWorld(state, slice, pos);
            }

            Arrays.fill(output, color);
        }
    }
}
