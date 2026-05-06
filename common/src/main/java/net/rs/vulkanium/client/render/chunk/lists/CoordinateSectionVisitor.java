package net.rs.vulkanium.client.render.chunk.lists;

public interface CoordinateSectionVisitor {
    void visit(int x, int y, int z);
}
