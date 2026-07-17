package com.example.examplemod.client.renderer;

import com.example.examplemod.entity.BlackManeHoundEntity;
import com.example.examplemod.entity.AncientCultistEntity;
import com.example.examplemod.entity.AltarCultistEntity;
import com.example.examplemod.entity.CultistEchoEntity;
import com.example.examplemod.entity.FrostShellSilverfishEntity;
import com.example.examplemod.entity.GoatHunterButcherEntity;
import com.example.examplemod.entity.StarManePegasusEntity;
import com.example.examplemod.entity.TormentedWraithEntity;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SimpleMobRenderers {
    public static class PegasusRenderer extends MobRenderer<StarManePegasusEntity, CowModel<StarManePegasusEntity>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/cow/cow.png");

        public PegasusRenderer(EntityRendererProvider.Context context) {
            super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7F);
        }

        @Override
        public ResourceLocation getTextureLocation(StarManePegasusEntity entity) {
            return TEXTURE;
        }
    }

    public static class FrostSilverfishRenderer extends MobRenderer<FrostShellSilverfishEntity, SilverfishModel<FrostShellSilverfishEntity>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/silverfish.png");

        public FrostSilverfishRenderer(EntityRendererProvider.Context context) {
            super(context, new SilverfishModel<>(context.bakeLayer(ModelLayers.SILVERFISH)), 0.3F);
        }

        @Override
        public ResourceLocation getTextureLocation(FrostShellSilverfishEntity entity) {
            return TEXTURE;
        }
    }

    public static class HoundRenderer extends MobRenderer<BlackManeHoundEntity, CowModel<BlackManeHoundEntity>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/wolf/wolf.png");

        public HoundRenderer(EntityRendererProvider.Context context) {
            super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(BlackManeHoundEntity entity) {
            return TEXTURE;
        }
    }

    public static class CultistEchoRenderer extends MobRenderer<CultistEchoEntity, HumanoidModel<CultistEchoEntity>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation("sevenstars", "textures/entity/cultist_echo.png");

        public CultistEchoRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(CultistEchoEntity entity) {
            return TEXTURE;
        }

        @Override
        protected RenderType getRenderType(CultistEchoEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
            return RenderType.entityTranslucent(TEXTURE);
        }
    }

    public static class AncientCultistRenderer extends MobRenderer<AncientCultistEntity, HumanoidModel<AncientCultistEntity>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation("sevenstars", "textures/entity/ancient_cultist.png");
        public AncientCultistRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        }
        @Override public ResourceLocation getTextureLocation(AncientCultistEntity entity) { return TEXTURE; }
    }

    public static class AltarCultistRenderer extends MobRenderer<AltarCultistEntity, HumanoidModel<AltarCultistEntity>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation("sevenstars", "textures/entity/altar_cultist.png");
        public AltarCultistRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.55F);
        }
        @Override public ResourceLocation getTextureLocation(AltarCultistEntity entity) { return TEXTURE; }
    }

    public static class ButcherRenderer extends MobRenderer<GoatHunterButcherEntity, HumanoidModel<GoatHunterButcherEntity>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation("sevenstars", "textures/entity/goat_hunter_butcher.png");

        public ButcherRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.6F);
        }

        @Override
        public ResourceLocation getTextureLocation(GoatHunterButcherEntity entity) {
            return TEXTURE;
        }
    }

    public static class WraithRenderer extends MobRenderer<TormentedWraithEntity, HumanoidModel<TormentedWraithEntity>> {
        private static final ResourceLocation TEXTURE = new ResourceLocation("sevenstars", "textures/entity/tormented_wraith.png");

        public WraithRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.45F);
        }

        @Override
        public ResourceLocation getTextureLocation(TormentedWraithEntity entity) {
            return TEXTURE;
        }
    }
}
