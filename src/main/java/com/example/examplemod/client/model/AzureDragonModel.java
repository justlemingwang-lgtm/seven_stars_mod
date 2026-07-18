package com.example.examplemod.client.model;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.AzureDragonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class AzureDragonModel extends HierarchicalModel<AzureDragonEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(ExampleMod.MODID, "azure_dragon"), "main");
    private final ModelPart root;
    private final ModelPart wingBlades;

    public AzureDragonModel(ModelPart root) {
        this.root = root;
        this.wingBlades = root.getChild("wing_blades");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-7.0F, -7.0F, -11.0F, 14.0F, 14.0F, 22.0F), PartPose.offset(0.0F, 9.0F, 0.0F));
        root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 40)
                .addBox(-4.0F, -4.0F, -10.0F, 8.0F, 8.0F, 12.0F), PartPose.offset(0.0F, 7.0F, -10.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(40, 40)
                .addBox(-5.0F, -4.0F, -8.0F, 10.0F, 8.0F, 10.0F)
                .texOffs(80, 0).addBox(-3.0F, -7.0F, -4.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(88, 0).addBox(1.0F, -7.0F, -4.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(0.0F, 6.0F, -20.0F));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 64)
                .addBox(0.0F, -1.0F, -2.0F, 22.0F, 2.0F, 14.0F, new CubeDeformation(0.15F)), PartPose.offset(6.0F, 5.0F, -4.0F));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 80)
                .addBox(-22.0F, -1.0F, -2.0F, 22.0F, 2.0F, 14.0F, new CubeDeformation(0.15F)), PartPose.offset(-6.0F, 5.0F, -4.0F));
        PartDefinition tail1 = root.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(72, 40)
                .addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 12.0F), PartPose.offset(0.0F, 9.0F, 10.0F));
        PartDefinition tail2 = tail1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(72, 60)
                .addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 12.0F), PartPose.offset(0.0F, 0.0F, 11.0F));
        tail2.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(72, 78)
                .addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 12.0F), PartPose.offset(0.0F, 0.0F, 11.0F));
        leg(root, "front_left_leg", 5.0F, -7.0F, false);
        leg(root, "front_right_leg", -5.0F, -7.0F, true);
        leg(root, "back_left_leg", 5.0F, 7.0F, false);
        leg(root, "back_right_leg", -5.0F, 7.0F, true);
        root.addOrReplaceChild("wing_blades", CubeListBuilder.create().texOffs(0, 100)
                .addBox(7.0F, -1.0F, -5.0F, 24.0F, 1.0F, 5.0F, new CubeDeformation(0.2F))
                .addBox(-31.0F, -1.0F, -5.0F, 24.0F, 1.0F, 5.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 4.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void leg(PartDefinition root, String name, float x, float z, boolean mirrored) {
        root.addOrReplaceChild(name, CubeListBuilder.create().mirror(mirrored).texOffs(104, 40)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F), PartPose.offset(x, 11.0F, z));
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(AzureDragonEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        wingBlades.visible = entity.hasWingBlades();
        animate(entity.idleAnimationState, AzureDragonAnimations.IDLE, ageInTicks);
        animate(entity.movementAnimationState, AzureDragonAnimations.MOVEMENT, ageInTicks);
        animate(entity.physicalBreathAnimationState, AzureDragonAnimations.PHYSICAL_BREATH, ageInTicks);
        animate(entity.stompAnimationState, AzureDragonAnimations.STOMP, ageInTicks);
        animate(entity.turningTailAnimationState, AzureDragonAnimations.TURNING_TAIL, ageInTicks);
        animate(entity.phaseTwoTransitionAnimationState, AzureDragonAnimations.PHASE_TWO_TRANSITION, ageInTicks);
        animate(entity.azureBreathAnimationState, AzureDragonAnimations.AZURE_BREATH, ageInTicks);
        animate(entity.divineTailAnimationState, AzureDragonAnimations.DIVINE_TAIL, ageInTicks);
        animate(entity.bullSlashWindupAnimationState, AzureDragonAnimations.BULL_SLASH_WINDUP, ageInTicks);
        animate(entity.bullSlashChaseAnimationState, AzureDragonAnimations.BULL_SLASH_CHASE, ageInTicks);
        animate(entity.bullSlashLeftAnimationState, AzureDragonAnimations.BULL_SLASH_LEFT, ageInTicks);
        animate(entity.bullSlashRightAnimationState, AzureDragonAnimations.BULL_SLASH_RIGHT, ageInTicks);
        animate(entity.phaseThreeTransitionAnimationState, AzureDragonAnimations.PHASE_THREE_TRANSITION, ageInTicks);
        animate(entity.defeatedAnimationState, AzureDragonAnimations.DEFEATED, ageInTicks);
        ModelPart head = root.getChild("head");
        head.yRot += netHeadYaw * ((float) Math.PI / 180.0F) * 0.35F;
        head.xRot += headPitch * ((float) Math.PI / 180.0F) * 0.25F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
