package net.rs.vulkanium.client.render.chunk.lists;

import net.rs.vulkanium.client.render.chunk.RenderSection;

public interface RenderSectionVisitor {
    void visit(RenderSection section);
}
