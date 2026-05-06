package net.rs.vulkanium.client.render.chunk.vertex.format.impl;

import net.rs.vulkanium.client.render.vertex.VertexFormatAttribute;
import net.rs.vulkanium.client.vk.attribute.ScalarType;

public class DefaultChunkMeshAttributes {
    public static final VertexFormatAttribute POSITION = new VertexFormatAttribute("POSITION", ScalarType.UNSIGNED_INT, 2, false, true);
    public static final VertexFormatAttribute COLOR = new VertexFormatAttribute("COLOR", ScalarType.UNSIGNED_BYTE, 4, true, false);
    public static final VertexFormatAttribute TEXTURE = new VertexFormatAttribute("TEXTURE", ScalarType.UNSIGNED_SHORT, 2, false, true);
    public static final VertexFormatAttribute LIGHT_MATERIAL_INDEX = new VertexFormatAttribute("LIGHT_MATERIAL_INDEX", ScalarType.UNSIGNED_BYTE, 4, false, true);
}
