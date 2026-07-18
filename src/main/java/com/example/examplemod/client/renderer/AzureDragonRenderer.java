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

public class AzureDragonRenderer extends MobRenderer<AzureDragonEntity, AzureDragonModel> {
    private static final ResourceLocation DRAGON_TEXTURE = new ResourceLocation("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation DRAGON_EYES = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");

    public AzureDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new AzureDragonModel(context.bakeLayer(AzureDragonModel.LAYER)), 3.0F);
        addLayer(new DragonEyesLayer(this));
    }

    private static final class DragonEyesLayer extends RenderLayer<AzureDragonEntity, AzureDragonModel> {
        private DragonEyesLayer(AzureDragonRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AzureDragonEntity entity,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                           float netHeadYaw, float headPitch) {
            getParentModel().renderToBuffer(poseStack, buffer.getBuffer(RenderType.eyes(DRAGON_EYES)),
                    15728640, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AzureDragonEntity entity) {
        return DRAGON_TEXTURE;
    }

    @Override
    public boolean shouldRender(AzureDragonEntity entity, net.minecraft.client.renderer.culling.Frustum frustum,
                                double x, double y, double z) {
        return super.shouldRender(entity, frustum, x, y, z) || entity.distanceToSqr(x, y, z) < 16384.0D;
    }
}
