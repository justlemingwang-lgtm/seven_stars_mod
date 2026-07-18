package com.example.examplemod.client.renderer;

import com.example.examplemod.client.model.AzureDragonModel;
import com.example.examplemod.entity.AzureDragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import com.example.examplemod.ExampleMod;

public class AzureDragonRenderer extends MobRenderer<AzureDragonEntity, AzureDragonModel> {
    private static final ResourceLocation PLACEHOLDER_TEXTURE = new ResourceLocation("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation FINAL_TEXTURE = new ResourceLocation(ExampleMod.MODID, "textures/entity/azure_dragon.png");
    private static final ResourceLocation FINAL_EMISSIVE_TEXTURE = new ResourceLocation(ExampleMod.MODID, "textures/entity/azure_dragon_emissive.png");

    public AzureDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new AzureDragonModel(context.bakeLayer(AzureDragonModel.LAYER)), 2.5F);
        addLayer(new EmissiveLayer(this));
    }

    private static final class EmissiveLayer extends RenderLayer<AzureDragonEntity, AzureDragonModel> {
        private EmissiveLayer(AzureDragonRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AzureDragonEntity entity,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                           float netHeadYaw, float headPitch) {
            if (Minecraft.getInstance().getResourceManager().getResource(FINAL_EMISSIVE_TEXTURE).isEmpty()) return;
            getParentModel().renderToBuffer(poseStack, buffer.getBuffer(RenderType.eyes(FINAL_EMISSIVE_TEXTURE)),
                    15728640, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AzureDragonEntity entity) {
        return Minecraft.getInstance().getResourceManager().getResource(FINAL_TEXTURE).isPresent()
                ? FINAL_TEXTURE : PLACEHOLDER_TEXTURE;
    }

    @Override
    public boolean shouldRender(AzureDragonEntity entity, net.minecraft.client.renderer.culling.Frustum frustum,
                                double x, double y, double z) {
        return super.shouldRender(entity, frustum, x, y, z) || entity.distanceToSqr(x, y, z) < 16384.0D;
    }
}
