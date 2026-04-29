package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModPotions {
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, ExampleMod.MODID);

    public static final RegistryObject<Potion> QI_SURGE = POTIONS.register("qi_surge",
            () -> new Potion(new MobEffectInstance(ModEffects.QI_SURGE.get(), 3600, 0)));
    public static final RegistryObject<Potion> STRONG_QI_SURGE = POTIONS.register("strong_qi_surge",
            () -> new Potion(new MobEffectInstance(ModEffects.QI_SURGE.get(), 1800, 1)));
    public static final RegistryObject<Potion> LONG_QI_SURGE = POTIONS.register("long_qi_surge",
            () -> new Potion(new MobEffectInstance(ModEffects.QI_SURGE.get(), 9600, 0)));
    public static final RegistryObject<Potion> QI_EXHAUSTION = POTIONS.register("qi_exhaustion",
            () -> new Potion(new MobEffectInstance(ModEffects.QI_EXHAUSTION.get(), 1800, 0)));
    public static final RegistryObject<Potion> LONG_QI_EXHAUSTION = POTIONS.register("long_qi_exhaustion",
            () -> new Potion(new MobEffectInstance(ModEffects.QI_EXHAUSTION.get(), 4800, 0)));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
