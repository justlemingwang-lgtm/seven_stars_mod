package com.example.examplemod;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.DoubleValue QINGLONG_ILLUSION_SHAKE_SCALE = BUILDER
            .comment("Camera shake intensity during the Azure Dragon phase-three illusion.")
            .defineInRange("qinglongIllusionShakeScale", 1.0D, 0.0D, 2.0D);
    public static final ForgeConfigSpec.BooleanValue DISABLE_QINGLONG_CAMERA_SHAKE = BUILDER
            .comment("Disable camera shake without disabling the lava illusion.")
            .define("disableQinglongCameraShake", false);
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {
    }
}
