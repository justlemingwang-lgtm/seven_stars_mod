package com.example.examplemod.client.model;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public final class AzureDragonAnimations {
    public static final AnimationDefinition IDLE = rotation("head", 2.0F, -3.0F, 3.0F, true);
    public static final AnimationDefinition MOVEMENT = rotation("body", 1.0F, -2.0F, 2.0F, true);
    public static final AnimationDefinition PHYSICAL_BREATH = rotation("head", 1.6F, -25.0F, 18.0F, false);
    public static final AnimationDefinition STOMP = rotation("front_right_leg", 1.4F, -55.0F, 10.0F, false);
    public static final AnimationDefinition TURNING_TAIL = rotation("tail_1", 1.2F, -40.0F, 50.0F, false);
    public static final AnimationDefinition PHASE_TWO_TRANSITION = rotation("left_wing", 4.0F, -15.0F, 55.0F, false);
    public static final AnimationDefinition AZURE_BREATH = rotation("head", 2.4F, -32.0F, 24.0F, false);
    public static final AnimationDefinition DIVINE_TAIL = rotation("tail_1", 3.0F, -65.0F, 70.0F, false);
    public static final AnimationDefinition BULL_SLASH_WINDUP = rotation("left_wing", 1.75F, -20.0F, 75.0F, false);
    public static final AnimationDefinition BULL_SLASH_CHASE = rotation("body", 2.25F, 10.0F, -8.0F, true);
    public static final AnimationDefinition BULL_SLASH_LEFT = rotation("left_wing", 0.3F, -75.0F, 60.0F, false);
    public static final AnimationDefinition BULL_SLASH_RIGHT = rotation("right_wing", 0.3F, 75.0F, -60.0F, false);
    public static final AnimationDefinition PHASE_THREE_TRANSITION = rotation("right_wing", 4.0F, 15.0F, -70.0F, false);
    public static final AnimationDefinition DEFEATED = rotation("body", 7.0F, 0.0F, 28.0F, false);

    private AzureDragonAnimations() {
    }

    private static AnimationDefinition rotation(String part, float length, float startDegrees,
                                                float endDegrees, boolean looping) {
        AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(length);
        if (looping) builder.looping();
        return builder.addAnimation(part, new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0.0F, KeyframeAnimations.degreeVec(startDegrees, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(length * 0.5F, KeyframeAnimations.degreeVec(endDegrees, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(length, KeyframeAnimations.degreeVec(startDegrees, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM))).build();
    }
}
