package net.rs.vulkanium.mixin.core.world.map;

import net.rs.vulkanium.client.render.chunk.map.ChunkStatus;
import net.rs.vulkanium.client.render.chunk.map.ChunkTracker;
import net.rs.vulkanium.client.render.chunk.map.ChunkTrackerHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;


@Mixin(ClientLevel.class)
public class ClientLevelMixin implements ChunkTrackerHolder {
    @Unique
    private final ChunkTracker chunkTracker = new ChunkTracker();

    @Override
    public ChunkTracker vulkanium$getTracker() {
        return Objects.requireNonNull(this.chunkTracker);
    }

    @Inject(method = "unload", at = @At("HEAD"))
    private void vulkanium$trackChunkUnload(LevelChunk levelChunk, CallbackInfo ci) {
        var pos = levelChunk.getPos();
        this.chunkTracker.onChunkStatusRemoved(pos.x(), pos.z(), ChunkStatus.FLAG_ALL);
    }
}
