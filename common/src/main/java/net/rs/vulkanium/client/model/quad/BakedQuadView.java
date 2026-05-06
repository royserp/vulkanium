package net.rs.vulkanium.client.model.quad;

import net.rs.vulkanium.client.model.quad.properties.ModelQuadFacing;

public interface BakedQuadView extends ModelQuadView {
    ModelQuadFacing getNormalFace();

    int getFaceNormal();

    boolean hasShade();

    boolean hasAO();
}
