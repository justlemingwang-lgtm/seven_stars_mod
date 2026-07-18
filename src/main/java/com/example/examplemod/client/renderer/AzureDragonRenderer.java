package com.example.examplemod.client.renderer;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.client.model.AzureDragonModel;
import com.example.examplemod.entity.AzureDragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AzureDragonRenderer extends MobRenderer<AzureDragonEntity, AzureDragonModel> {
    private static final ResourceLocation DRAGON_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ExampleMod.MODID, "textures/entity/azure_dragon.png");

    public AzureDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new AzureDragonModel(context.bakeLayer(AzureDragonModel.LAYER)), 3.0F);
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
