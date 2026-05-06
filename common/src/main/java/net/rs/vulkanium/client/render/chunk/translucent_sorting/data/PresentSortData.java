package net.rs.vulkanium.client.render.chunk.translucent_sorting.data;

import net.rs.vulkanium.client.util.NativeBuffer;

import java.nio.IntBuffer;

public interface PresentSortData {
    NativeBuffer getIndexBuffer();

    default IntBuffer getIntBuffer() {
        return this.getIndexBuffer().getDirectBuffer().asIntBuffer();
    }
}
