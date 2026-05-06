package net.rs.vulkanium.client.util;

import org.joml.Matrix4fc;

public interface GameRendererStorage extends FogStorage {
    Matrix4fc vulkanium$getProjectionMatrix();
}
