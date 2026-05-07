package net.rs.vulkanium.client.render.chunk;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import net.rs.vulkanium.client.VulkaniumClientMod;
import net.rs.vulkanium.client.model.quad.properties.ModelQuadFacing;
import net.rs.vulkanium.client.render.chunk.data.SectionRenderDataStorage;
import net.rs.vulkanium.client.render.chunk.data.SectionRenderDataUnsafe;
import net.rs.vulkanium.client.render.chunk.lists.ChunkRenderList;
import net.rs.vulkanium.client.render.chunk.lists.ChunkRenderListIterable;
import net.rs.vulkanium.client.render.chunk.region.RenderRegion;
import net.rs.vulkanium.client.render.chunk.shader.ChunkShaderInterface;
import net.rs.vulkanium.client.render.chunk.shader.DefaultShaderInterface;
import net.rs.vulkanium.client.render.chunk.terrain.TerrainRenderPass;
import net.rs.vulkanium.client.render.chunk.vertex.format.ChunkVertexType;
import net.rs.vulkanium.client.render.viewport.CameraTransform;
import net.rs.vulkanium.client.util.BitwiseMath;
import net.rs.vulkanium.client.util.FogParameters;
import net.rs.vulkanium.client.util.UInt32;
import net.rs.vulkanium.client.vk.Blaze3DAccess;
import net.rs.vulkanium.client.vk.buffer.VkBuffer;
import net.rs.vulkanium.client.vk.buffer.VkIndexType;
import net.rs.vulkanium.client.vk.device.CommandList;
import net.rs.vulkanium.client.vk.device.MultiDrawBatch;
import net.rs.vulkanium.client.vk.device.RenderDevice;
import net.rs.vulkanium.client.vk.renderpass.VulkanRenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.Iterator;

public class DefaultChunkRenderer extends ShaderChunkRenderer {
    private static final int MODEL_UNASSIGNED = ModelQuadFacing.UNASSIGNED.ordinal();
    private static final int MODEL_POS_X = ModelQuadFacing.POS_X.ordinal();
    private static final int MODEL_POS_Y = ModelQuadFacing.POS_Y.ordinal();
    private static final int MODEL_POS_Z = ModelQuadFacing.POS_Z.ordinal();
    private static final int MODEL_NEG_X = ModelQuadFacing.NEG_X.ordinal();
    private static final int MODEL_NEG_Y = ModelQuadFacing.NEG_Y.ordinal();
    private static final int MODEL_NEG_Z = ModelQuadFacing.NEG_Z.ordinal();
    private final SharedQuadIndexBuffer sharedIndexBuffer;
    public DefaultChunkRenderer(RenderDevice device, ChunkVertexType vertexType) {
        super(device, vertexType);

        this.sharedIndexBuffer = new SharedQuadIndexBuffer(device.createCommandList(), SharedQuadIndexBuffer.IndexType.INTEGER);
    }

    private static void fillCommandBuffer(MultiDrawBatch batch,
                                          RenderRegion renderRegion,
                                          SectionRenderDataStorage renderDataStorage,
                                          ChunkRenderList renderList,
                                          CameraTransform camera,
                                          TerrainRenderPass pass,
                                          boolean useBlockFaceCulling,
                                          boolean useIndexedTessellation) {
        batch.isFilled = true;

        var iterator = renderList.sectionsWithGeometryIterator(pass.isTranslucent());

        if (iterator == null) {
            return;
        }

        // The origin of the chunk in world space
        int originX = renderRegion.getChunkX();
        int originY = renderRegion.getChunkY();
        int originZ = renderRegion.getChunkZ();

        while (iterator.hasNext()) {
            int sectionIndex = iterator.nextByteAsInt();

            var pMeshData = renderDataStorage.getDataPointer(sectionIndex);

            int chunkX = originX + LocalSectionIndex.unpackX(sectionIndex);
            int chunkY = originY + LocalSectionIndex.unpackY(sectionIndex);
            int chunkZ = originZ + LocalSectionIndex.unpackZ(sectionIndex);

            // The bit field of "visible" geometry sets which should be rendered
            int slices;

            if (useBlockFaceCulling) {
                slices = getVisibleFaces(camera.intX, camera.intY, camera.intZ, chunkX, chunkY, chunkZ);
            } else {
                slices = ModelQuadFacing.ALL;
            }

            // Mask off any geometry sets which are empty (contain no geometry)
            slices &= SectionRenderDataUnsafe.getSliceMask(pMeshData);

            // If there are no geometry sets to render, don't try to build a draw command buffer for this section
            if (slices == 0) {
                continue;
            }

            if (useIndexedTessellation && SectionRenderDataUnsafe.isLocalIndex(pMeshData)) {
                addLocalIndexedDrawCommands(batch, pMeshData, slices);
            } else {
                addSharedIndexedDrawCommands(batch, pMeshData, slices);
            }
        }
    }

    @SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
    private static void addLocalIndexedDrawCommands(MultiDrawBatch batch, long pMeshData, int mask) {
        int size = batch.size;

        long elementOffset = SectionRenderDataUnsafe.getBaseElement(pMeshData);
        long baseVertex = SectionRenderDataUnsafe.getBaseVertex(pMeshData);

        for (int facing = 0; facing < ModelQuadFacing.COUNT; facing++) {
            final long vertexCount = SectionRenderDataUnsafe.getVertexCount(pMeshData, facing);
            final long indexCount = (vertexCount >> 2) * 6;

            if (((mask >> facing) & 1) != 0) {
                batch.addDraw(UInt32.uncheckedDowncast(indexCount), UInt32.uncheckedDowncast(elementOffset), UInt32.uncheckedDowncast(baseVertex));
                size++;
            }

            baseVertex += vertexCount;
            elementOffset += indexCount;
        }

        batch.size = size;
    }

    @SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
    private static void addSharedIndexedDrawCommands(MultiDrawBatch batch, long pMeshData, int mask) {

        final long elementOffset = SectionRenderDataUnsafe.getBaseElement(pMeshData);
        final var facingList = SectionRenderDataUnsafe.getFacingList(pMeshData);

        int size = batch.size;
        long groupVertexCount = 0;
        long baseVertex = SectionRenderDataUnsafe.getBaseVertex(pMeshData);
        int lastMaskBit = 0;

        for (int i = 0; i <= ModelQuadFacing.COUNT; i++) {

            int maskBit = 0;
            long vertexCount = 0;

            if (i < ModelQuadFacing.COUNT) {
                vertexCount = SectionRenderDataUnsafe.getVertexCount(pMeshData, i);

                if (vertexCount != 0) {
                    var facing = (facingList >>> (i * 8)) & 0xFF;
                    maskBit = (mask >>> facing) & 1;
                }
            }

            if (maskBit == 0) {
                if (lastMaskBit == 1) {
                    if (i < ModelQuadFacing.COUNT && vertexCount == 0) {
                        continue;
                    }

                    long indexCount = (groupVertexCount >> 2) * 6;

                    batch.addDraw(UInt32.uncheckedDowncast(indexCount), UInt32.uncheckedDowncast(elementOffset), UInt32.uncheckedDowncast(baseVertex));

                    size++;

                    baseVertex += groupVertexCount;
                    groupVertexCount = 0;
                }

                baseVertex += vertexCount;
            } else {
                groupVertexCount += vertexCount;
            }

            lastMaskBit = maskBit;
        }

        batch.size = size;
    }

    public static int getVisibleFaces(int originX, int originY, int originZ, int chunkX, int chunkY, int chunkZ) {
        int boundsMinX = (chunkX << 4), boundsMaxX = boundsMinX + 16;
        int boundsMinY = (chunkY << 4), boundsMaxY = boundsMinY + 16;
        int boundsMinZ = (chunkZ << 4), boundsMaxZ = boundsMinZ + 16;

        int planes = (1 << MODEL_UNASSIGNED);

        planes |= BitwiseMath.greaterThan(originX, (boundsMinX - 3)) << MODEL_POS_X;
        planes |= BitwiseMath.greaterThan(originY, (boundsMinY - 3)) << MODEL_POS_Y;
        planes |= BitwiseMath.greaterThan(originZ, (boundsMinZ - 3)) << MODEL_POS_Z;

        planes |= BitwiseMath.lessThan(originX, (boundsMaxX + 3)) << MODEL_NEG_X;
        planes |= BitwiseMath.lessThan(originY, (boundsMaxY + 3)) << MODEL_NEG_Y;
        planes |= BitwiseMath.lessThan(originZ, (boundsMaxZ + 3)) << MODEL_NEG_Z;

        return planes;
    }

    private static void setModelMatrixUniforms(ChunkShaderInterface shader, RenderRegion region, CameraTransform camera, VkBuffer chunKData) {
        float x = getCameraTranslation(region.getOriginX(), camera.intX, camera.fracX);
        float y = getCameraTranslation(region.getOriginY(), camera.intY, camera.fracY);
        float z = getCameraTranslation(region.getOriginZ(), camera.intZ, camera.fracZ);

        shader.setRegionOffset(x, y, z);
        shader.setChunkData(chunKData, Math.toIntExact(System.currentTimeMillis() - region.getCreationTime()));
    }

    private static float getCameraTranslation(int chunkBlockPos, int cameraBlockPos, float cameraPos) {
        return (chunkBlockPos - cameraBlockPos) - cameraPos;
    }

    @Override
    public void render(ChunkRenderMatrices matrices,
                       CommandList commandList,
                       ChunkRenderListIterable renderLists,
                       TerrainRenderPass renderPass,
                       CameraTransform camera,
                       FogParameters parameters,
                       boolean indexedRenderingEnabled,
                       GpuSampler terrainSampler,
                       VulkanRenderPass externalPass) {
        final boolean useBlockFaceCulling = VulkaniumClientMod.options().performance.useBlockFaceCulling;
        final boolean useIndexedTessellation = renderPass.isTranslucent() && indexedRenderingEnabled;

        Iterator<ChunkRenderList> iterator = renderLists.iterator(renderPass.isTranslucent());

        while (iterator.hasNext()) {
            ChunkRenderList renderList = iterator.next();

            var region = renderList.getRegion();
            var storage = region.getStorage(renderPass);

            if (storage == null) {
                continue;
            }

            var batch = region.getCachedBatch(renderPass);
            if (!batch.isFilled) {
                fillCommandBuffer(batch, region, storage, renderList, camera, renderPass, useBlockFaceCulling, useIndexedTessellation);
            }

            if (batch.isEmpty()) {
                continue;
            }

            if (!useIndexedTessellation) {
                this.sharedIndexBuffer.ensureCapacity(commandList, batch.getIndexBufferSize());
            }
        }

        if (externalPass != null) {
            this.renderInternal(matrices, commandList, renderLists, renderPass, camera, parameters, useIndexedTessellation, terrainSampler, externalPass);
        } else {
            try (VulkanRenderPass pass = commandList.startRenderPass(Blaze3DAccess.getView(renderPass.getTarget().getColorTextureView()))) {
                this.renderInternal(matrices, commandList, renderLists, renderPass, camera, parameters, useIndexedTessellation, terrainSampler, pass);
            }
        }
    }

    private void renderInternal(ChunkRenderMatrices matrices,
                                CommandList commandList,
                                ChunkRenderListIterable renderLists,
                                TerrainRenderPass renderPass,
                                CameraTransform camera,
                                FogParameters parameters,
                                boolean useIndexedTessellation,
                                GpuSampler terrainSampler,
                                VulkanRenderPass pass) {
        super.begin(pass, renderPass, parameters, terrainSampler);

        ChunkShaderInterface shader = this.activeProgram.getInterface();
        shader.setProjectionMatrix(matrices.projection());
        shader.setModelViewMatrix(matrices.modelView());

        var iterator = renderLists.iterator(renderPass.isTranslucent());

        while (iterator.hasNext()) {
            ChunkRenderList renderList = iterator.next();

            var region = renderList.getRegion();
            var storage = region.getStorage(renderPass);

            if (storage == null) {
                continue;
            }

            var batch = region.getCachedBatch(renderPass);

            if (batch.isEmpty()) {
                continue;
            }

            setModelMatrixUniforms(shader, region, camera, region.getResources().prepareChunkData(commandList));

            try (MemoryStack stack = MemoryStack.stackPush()) {
                long pushData = stack.nmalloc(DefaultShaderInterface.PUSH_CONSTANT_SIZE);
                this.activeProgram.getInterface().fillPushConstants(pushData);

                pass.pushConstants(this.activeProgram, pushData, DefaultShaderInterface.PUSH_CONSTANT_SIZE);
                VkWriteDescriptorSet.Buffer buf = VkWriteDescriptorSet.calloc(2, stack);
                buf.sType$Default().dstSet(0).dstBinding(0).descriptorCount(1).descriptorType(VK13.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).dstArrayElement(0);
                buf.pImageInfo(VkDescriptorImageInfo.calloc(1, stack)
                        .imageView(Blaze3DAccess.getView(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getTextureView()))
                        .imageLayout(VK13.VK_IMAGE_LAYOUT_GENERAL)
                        .sampler(Blaze3DAccess.getSampler(RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, true)))
                );
                buf.position(1);
                buf.sType$Default().dstSet(0).dstBinding(1).descriptorCount(1).descriptorType(VK13.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).dstArrayElement(0);
                buf.pImageInfo(VkDescriptorImageInfo.calloc(1, stack)
                        .imageView(Blaze3DAccess.getView(Minecraft.getInstance().gameRenderer.lightmap()))
                        .imageLayout(VK13.VK_IMAGE_LAYOUT_GENERAL)
                        .sampler(Blaze3DAccess.getSampler(RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false)))
                );
                buf.position(0);
                pass.pushDescriptors(this.activeProgram, buf);
            }

            pass.bindVertexBuffer(region.getResources().getGeometryBuffer());
            pass.bindIndexBuffer(useIndexedTessellation ? region.getResources().getIndexBuffer() : sharedIndexBuffer.getBufferObject(), VkIndexType.UNSIGNED_INT);

            pass.draw(batch);
        }

        super.end(renderPass);
    }

    @Override
    public void delete(CommandList commandList) {
        super.delete(commandList);

        this.sharedIndexBuffer.delete(commandList);
    }
}
