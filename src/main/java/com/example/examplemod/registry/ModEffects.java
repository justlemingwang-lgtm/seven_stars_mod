package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.effect.SimpleQiEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ExampleMod.MODID);

    public static final RegistryObject<MobEffect> QI_SURGE = EFFECTS.register("qi_surge",
            () -> new SimpleQiEffect(MobEffectCategory.BENEFICIAL, 0x66DDAA));
    public static final RegistryObject<MobEffect> QI_EXHAUSTION = EFFECTS.register("qi_exhaustion",
            () -> new SimpleQiEffect(MobEffectCategory.HARMFUL, 0x6F7FA8));
    public static final RegistryObject<MobEffect> CRACKED_CLAW_COMBO = EFFECTS.register("cracked_claw_combo",
            () -> new SimpleQiEffect(MobEffectCategory.BENEFICIAL, 0xB03030));

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
