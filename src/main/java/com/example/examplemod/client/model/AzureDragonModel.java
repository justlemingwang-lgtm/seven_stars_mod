package com.example.examplemod.client.model;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.AzureDragonEntity;
import com.example.examplemod.stage3.AzureDragonAttackType;
import com.example.examplemod.stage3.Stage3Constants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Vanilla Ender Dragon geometry, adapted to the Azure Dragon combat state machine. */
public final class AzureDragonModel extends EntityModel<AzureDragonEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ExampleMod.MODID, "azure_dragon"), "main");

    private final ModelPart head;
    private final ModelPart neck;
    private final ModelPart jaw;
    private final ModelPart body;
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;
    private final ModelPart leftFrontLeg;
    private final ModelPart leftFrontLegTip;
    private final ModelPart leftFrontFoot;
    private final ModelPart leftRearLeg;
    private final ModelPart leftRearLegTip;
    private final ModelPart leftRearFoot;
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;
    private final ModelPart rightFrontLeg;
    private final ModelPart rightFrontLegTip;
    private final ModelPart rightFrontFoot;
    private final ModelPart rightRearLeg;
    private final ModelPart rightRearLegTip;
    private final ModelPart rightRearFoot;
    private AzureDragonEntity entity;
    private float partialTick;

    public AzureDragonModel(ModelPart root) {
        head = root.getChild("head");
        jaw = head.getChild("jaw");
        neck = root.getChild("neck");
        body = root.getChild("body");
        leftWing = root.getChild("left_wing");
        leftWingTip = leftWing.getChild("left_wing_tip");
        leftFrontLeg = root.getChild("left_front_leg");
        leftFrontLegTip = leftFrontLeg.getChild("left_front_leg_tip");
        leftFrontFoot = leftFrontLegTip.getChild("left_front_foot");
        leftRearLeg = root.getChild("left_hind_leg");
        leftRearLegTip = leftRearLeg.getChild("left_hind_leg_tip");
        leftRearFoot = leftRearLegTip.getChild("left_hind_foot");
        rightWing = root.getChild("right_wing");
        rightWingTip = rightWing.getChild("right_wing_tip");
        rightFrontLeg = root.getChild("right_front_leg");
        rightFrontLegTip = rightFrontLeg.getChild("right_front_leg_tip");
        rightFrontFoot = rightFrontLegTip.getChild("right_front_foot");
        rightRearLeg = root.getChild("right_hind_leg");
        rightRearLegTip = rightRearLeg.getChild("right_hind_leg_tip");
        rightRearFoot = rightRearLegTip.getChild("right_hind_foot");
    }

    public static LayerDefinition createBodyLayer() {
        return EnderDragonRenderer.createBodyLayer();
    }

    @Override
    public void prepareMobModel(AzureDragonEntity dragon, float limbSwing, float limbSwingAmount, float partialTick) {
        entity = dragon;
        this.partialTick = partialTick;
    }

    @Override
    public void setupAnim(AzureDragonEntity dragon, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        entity = dragon;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        if (entity == null) return;
        poseStack.pushPose();
        float flap = Mth.lerp(partialTick, entity.previousFlapTime, entity.flapTime);
        float cycle = flap * Mth.TWO_PI;
        float bob = (float) (Math.sin(cycle - 1.0F) + 1.0D);
        bob = (bob * bob + bob * 2.0F) * 0.05F;
        boolean warning = entity.getAttackType() != AzureDragonAttackType.NONE
                && entity.getAttackTick() < windup(entity.getAttackType());
        jaw.xRot = warning ? 0.65F : (float) (Math.sin(cycle) + 1.0D) * 0.2F;

        poseStack.translate(0.0F, bob - 2.0F, -3.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(bob * 2.0F));
        float x = 0.0F;
        float y = 20.0F;
        float z = -12.0F;
        float turnWave = Mth.sin((entity.tickCount + partialTick) * 0.045F) * 0.12F;

        for (int index = 0; index < 5; index++) {
            float wave = Mth.cos(index * 0.45F + cycle) * 0.15F;
            neck.yRot = turnWave * (index + 1) * 0.22F;
            neck.xRot = wave;
            neck.zRot = -turnWave * 0.45F;
            neck.x = x;
            neck.y = y;
            neck.z = z;
            y += Mth.sin(neck.xRot) * 10.0F;
            z -= Mth.cos(neck.yRot) * Mth.cos(neck.xRot) * 10.0F;
            x -= Mth.sin(neck.yRot) * Mth.cos(neck.xRot) * 10.0F;
            neck.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        head.x = x;
        head.y = y;
        head.z = z;
        head.yRot = turnWave * 1.5F;
        head.xRot = entity.getAttackType() == AzureDragonAttackType.CHARGE ? 0.22F
                : entity.getAttackType() == AzureDragonAttackType.AERIAL_SLAM ? -0.2F
                : entity.getAttackType() == AzureDragonAttackType.PHYSICAL_BREATH
                || entity.getAttackType() == AzureDragonAttackType.AZURE_BREATH
                ? entity.getXRot() * Mth.DEG_TO_RAD : 0.0F;
        head.zRot = -turnWave;
        head.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);

        poseStack.pushPose();
        poseStack.translate(0.0F, 1.0F, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-turnWave * 35.0F));
        poseStack.translate(0.0F, -1.0F, 0.0F);
        body.zRot = 0.0F;
        body.xRot = entity.getAttackType() == AzureDragonAttackType.AERIAL_SLAM
                && entity.getAttackTick() >= Stage3Constants.AERIAL_SLAM_WINDUP ? -0.35F : 0.0F;
        body.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);

        float wingSpeed = entity.getAttackType() == AzureDragonAttackType.AERIAL_SLAM ? 1.8F : 1.0F;
        float wingCycle = cycle * wingSpeed;
        leftWing.xRot = 0.125F - Mth.cos(wingCycle) * 0.2F;
        leftWing.yRot = -0.25F;
        leftWing.zRot = -(Mth.sin(wingCycle) + 0.125F) * 0.8F;
        leftWingTip.zRot = (Mth.sin(wingCycle + 2.0F) + 0.5F) * 0.75F;
        rightWing.xRot = leftWing.xRot;
        rightWing.yRot = -leftWing.yRot;
        rightWing.zRot = -leftWing.zRot;
        rightWingTip.zRot = -leftWingTip.zRot;
        renderSide(poseStack, consumer, packedLight, packedOverlay, bob, leftWing, leftFrontLeg,
                leftFrontLegTip, leftFrontFoot, leftRearLeg, leftRearLegTip, leftRearFoot,
                red, green, blue, alpha);
        renderSide(poseStack, consumer, packedLight, packedOverlay, bob, rightWing, rightFrontLeg,
                rightFrontLegTip, rightFrontFoot, rightRearLeg, rightRearLegTip, rightRearFoot,
                red, green, blue, alpha);
        poseStack.popPose();

        float tailPitch = 0.0F;
        y = 10.0F;
        z = 60.0F;
        x = 0.0F;
        for (int index = 0; index < 12; index++) {
            tailPitch += Mth.sin(index * 0.45F + cycle) * 0.05F;
            neck.yRot = Mth.PI + Mth.sin((entity.tickCount + partialTick) * 0.04F + index * 0.32F) * 0.12F;
            neck.xRot = tailPitch;
            neck.zRot = turnWave * 0.65F;
            neck.x = x;
            neck.y = y;
            neck.z = z;
            y += Mth.sin(neck.xRot) * 10.0F;
            z -= Mth.cos(neck.yRot) * Mth.cos(neck.xRot) * 10.0F;
            x -= Mth.sin(neck.yRot) * Mth.cos(neck.xRot) * 10.0F;
            neck.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        poseStack.popPose();
    }

    private static int windup(AzureDragonAttackType type) {
        return switch (type) {
            case PHYSICAL_BREATH -> Stage3Constants.PHYSICAL_BREATH_WINDUP;
            case STOMP -> Stage3Constants.STOMP_WINDUP;
            case TURNING_TAIL -> Stage3Constants.TURNING_TAIL_WINDUP;
            case AZURE_BREATH -> Stage3Constants.AZURE_BREATH_WINDUP;
            case DIVINE_TAIL -> Stage3Constants.DIVINE_TAIL_WINDUP;
            case BULL_AZURE_SLASH -> Stage3Constants.BULL_SLASH_WINDUP;
            case AERIAL_SLAM -> Stage3Constants.AERIAL_SLAM_WINDUP;
            case CHARGE -> Stage3Constants.CHARGE_WINDUP;
            case BITE -> Stage3Constants.BITE_WINDUP;
            default -> 0;
        };
    }

    private static void renderSide(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                                   float bob, ModelPart wing, ModelPart frontLeg, ModelPart frontLegTip,
                                   ModelPart frontFoot, ModelPart rearLeg, ModelPart rearLegTip, ModelPart rearFoot,
                                   float red, float green, float blue, float alpha) {
        rearLeg.xRot = 1.0F + bob * 0.1F;
        rearLegTip.xRot = 0.5F + bob * 0.1F;
        rearFoot.xRot = 0.75F + bob * 0.1F;
        frontLeg.xRot = 1.3F + bob * 0.1F;
        frontLegTip.xRot = -0.5F - bob * 0.1F;
        frontFoot.xRot = 0.75F + bob * 0.1F;
        wing.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
        frontLeg.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
        rearLeg.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
