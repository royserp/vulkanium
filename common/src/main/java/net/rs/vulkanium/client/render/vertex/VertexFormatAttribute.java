package net.rs.vulkanium.client.render.vertex;

import net.rs.vulkanium.client.vk.attribute.ScalarType;

public record VertexFormatAttribute(String name, ScalarType format, int count, boolean normalized, boolean intType) {

}