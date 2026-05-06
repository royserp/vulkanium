package net.rs.vulkanium.client.render.chunk.compile.executor;

import net.rs.vulkanium.client.render.chunk.compile.ChunkBuildContext;
import net.rs.vulkanium.client.util.task.CancellationToken;

public interface ChunkJob extends CancellationToken {
    void execute(ChunkBuildContext context);

    boolean isStarted();
    
    boolean isBlocking();

    long getEstimatedSize();

    long getEstimatedDuration();
    
    long getEstimatedUploadDuration();
}
