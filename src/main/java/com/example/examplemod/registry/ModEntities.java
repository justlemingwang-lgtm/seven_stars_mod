package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.BlackManeHoundEntity;
import com.example.examplemod.entity.FrostMobProjectileEntity;
import com.example.examplemod.entity.FrostShellSilverfishEntity;
import com.example.examplemod.entity.GoldenFingerProjectileEntity;
import com.example.examplemod.entity.GoatHornSpikeProjectileEntity;
import com.example.examplemod.entity.CultistEchoEntity;
import com.example.examplemod.entity.GoatHunterButcherEntity;
import com.example.examplemod.entity.IceDipperProjectileEntity;
import com.example.examplemod.entity.LightWaveProjectileEntity;
import com.example.examplemod.entity.StarManePegasusEntity;
import com.example.examplemod.entity.TormentedWraithEntity;
import com.example.examplemod.entity.TriangleShardProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ExampleMod.MODID);

    public static final RegistryObject<EntityType<LightWaveProjectileEntity>> LIGHT_WAVE_PROJECTILE = ENTITIES.register("light_wave_projectile",
            () -> EntityType.Builder.<LightWaveProjectileEntity>of(LightWaveProjectileEntity::new, MobCategory.MISC)
                    .sized(0.45F, 0.45F)
                    .clientTrackingRange(4)
                    .updateInterval(2)
                    .build("light_wave_projectile"));

    public static final RegistryObject<EntityType<GoldenFingerProjectileEntity>> GOLDEN_FINGER_PROJECTILE = ENTITIES.register("golden_finger_projectile",
            () -> EntityType.Builder.<GoldenFingerProjectileEntity>of(GoldenFingerProjectileEntity::new, MobCategory.MISC)
                    .sized(0.45F, 0.45F)
                    .clientTrackingRange(4)
                    .updateInterval(2)
                    .build("golden_finger_projectile"));

    public static final RegistryObject<EntityType<IceDipperProjectileEntity>> ICE_DIPPER_PROJECTILE = ENTITIES.register("ice_dipper_projectile",
            () -> EntityType.Builder.<IceDipperProjectileEntity>of(IceDipperProjectileEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(4)
                    .updateInterval(2)
                    .build("ice_dipper_projectile"));

    public static final RegistryObject<EntityType<FrostMobProjectileEntity>> FROST_MOB_PROJECTILE = ENTITIES.register("frost_mob_projectile",
            () -> EntityType.Builder.<FrostMobProjectileEntity>of(FrostMobProjectileEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(4)
                    .updateInterval(2)
                    .build("frost_mob_projectile"));

    public static final RegistryObject<EntityType<GoatHornSpikeProjectileEntity>> GOAT_HORN_SPIKE_PROJECTILE = ENTITIES.register("goat_horn_spike_projectile",
            () -> EntityType.Builder.<GoatHornSpikeProjectileEntity>of(GoatHornSpikeProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(2)
                    .build("goat_horn_spike_projectile"));

    public static final RegistryObject<EntityType<TriangleShardProjectileEntity>> TRIANGLE_SHARD_PROJECTILE = ENTITIES.register("triangle_shard_projectile",
            () -> EntityType.Builder.<TriangleShardProjectileEntity>of(TriangleShardProjectileEntity::new, MobCategory.MISC)
                    .sized(0.22F, 0.22F)
                    .clientTrackingRange(4)
                    .updateInterval(2)
                    .build("triangle_shard_projectile"));

    public static final RegistryObject<EntityType<StarManePegasusEntity>> STAR_MANE_PEGASUS = ENTITIES.register("star_mane_pegasus",
            () -> EntityType.Builder.of(StarManePegasusEntity::new, MobCategory.CREATURE)
                    .sized(1.4F, 1.6F)
                    .clientTrackingRange(8)
                    .build("star_mane_pegasus"));

    public static final RegistryObject<EntityType<FrostShellSilverfishEntity>> FROST_SHELL_SILVERFISH = ENTITIES.register("frost_shell_silverfish",
            () -> EntityType.Builder.of(FrostShellSilverfishEntity::new, MobCategory.MONSTER)
                    .sized(0.45F, 0.3F)
                    .clientTrackingRange(8)
                    .build("frost_shell_silverfish"));

    public static final RegistryObject<EntityType<BlackManeHoundEntity>> BLACK_MANE_HOUND = ENTITIES.register("black_mane_hound",
            () -> EntityType.Builder.of(BlackManeHoundEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.85F)
                    .clientTrackingRange(8)
                    .build("black_mane_hound"));

    public static final RegistryObject<EntityType<CultistEchoEntity>> CULTIST_ECHO = ENTITIES.register("cultist_echo",
            () -> EntityType.Builder.of(CultistEchoEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build("cultist_echo"));

    public static final RegistryObject<EntityType<GoatHunterButcherEntity>> GOAT_HUNTER_BUTCHER = ENTITIES.register("goat_hunter_butcher",
            () -> EntityType.Builder.of(GoatHunterButcherEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.0F)
                    .clientTrackingRange(8)
                    .build("goat_hunter_butcher"));

    public static final RegistryObject<EntityType<TormentedWraithEntity>> TORMENTED_WRAITH = ENTITIES.register("tormented_wraith",
            () -> EntityType.Builder.of(TormentedWraithEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.9F)
                    .clientTrackingRange(8)
                    .build("tormented_wraith"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
