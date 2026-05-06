package net.rs.vulkanium.mixin.frapi;

import net.rs.vulkanium.client.render.frapi.wrapper.ExtendedQuadViewImpl;
import net.rs.vulkanium.client.render.frapi.wrapper.QuadViewWrapper;
import net.rs.vulkanium.client.render.model.QuadViewImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(QuadViewImpl.class)
public class QuadViewImplMixin implements ExtendedQuadViewImpl {
    @Unique
    private QuadViewWrapper wrapper;

    @Override
    public QuadViewWrapper getWrapper() {
        if (wrapper == null) wrapper = new QuadViewWrapper((QuadViewImpl) (Object) this);
        return wrapper;
    }
}
