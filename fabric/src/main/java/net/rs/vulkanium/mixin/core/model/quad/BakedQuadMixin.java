package net.rs.vulkanium.mixin.core.model.quad;

import net.rs.vulkanium.client.model.quad.BakedQuadView;
import net.rs.vulkanium.client.model.quad.properties.ModelQuadFacing;
import net.rs.vulkanium.client.model.quad.properties.ModelQuadFlags;
import net.rs.vulkanium.api.util.NormI8;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BakedQuad.class)
public abstract class BakedQuadMixin implements BakedQuadView {
    @Shadow
    public abstract Direction direction();

    @Shadow
    public abstract BakedQuad.MaterialInfo materialInfo();

    @Shadow
    public abstract Vector3fc position(int i);

    @Shadow
    public abstract long packedUV(int i);

    @Unique
    private int flags;

    @Unique
    private int normal;

    @Unique
    private ModelQuadFacing normalFace = null;

    @Override
    public int getLightEmission() {
        return this.materialInfo().lightEmission();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Vector3fc position0, Vector3fc position1, Vector3fc position2, Vector3fc position3, long packedUV0, long packedUV1, long packedUV2, long packedUV3, Direction direction, BakedQuad.MaterialInfo materialInfo, CallbackInfo ci) {
        this.normal = this.calculateNormal();
        this.normalFace = ModelQuadFacing.fromPackedNormal(this.normal);

        this.flags = ModelQuadFlags.getQuadFlags(this, direction);
    }

    @Override
    public float getX(int idx) {
        return this.position(idx).x();
    }

    @Override
    public float getY(int idx) {
        return this.position(idx).y();
    }

    @Override
    public float getZ(int idx) {
        return this.position(idx).z();
    }

    @Override
    public int getColor(int idx) {
        return 0xFFFFFFFF;
    }

    @Override
    public int getVertexNormal(int idx) {
        return 0;
    }

    @Override
    public int getLight(int idx) {
        return 0;
    }

    @Override
    public TextureAtlasSprite getSprite() {
        return this.materialInfo().sprite();
    }

    @Override
    public float getTexU(int idx) {
        return UVPair.unpackU(this.packedUV(idx));
    }

    @Override
    public float getTexV(int idx) {
        return UVPair.unpackV(this.packedUV(idx));
    }

    @Override
    public int getFlags() {
        return this.flags;
    }

    @Override
    public int getTintIndex() {
        return this.materialInfo().tintIndex();
    }

    @Override
    public ModelQuadFacing getNormalFace() {
        return this.normalFace;
    }

    @Override
    public int getFaceNormal() {
        return this.normal;
    }

    @Override
    public Direction getLightFace() {
        return this.direction();
    }

    @Override
    public int getMaxLightQuad(int idx) {
        return LightCoordsUtil.lightCoordsWithEmission(getLight(idx), getLightEmission());
    }

    @Override
    public boolean hasShade() {
        return this.materialInfo().shade();
    }

    @Override
    public boolean hasAO() {
        return true;
    }
}
