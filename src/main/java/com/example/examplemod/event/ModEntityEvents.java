package com.example.examplemod.event;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.BlackManeHoundEntity;
import com.example.examplemod.entity.AncientCultistEntity;
import com.example.examplemod.entity.AltarCultistEntity;
import com.example.examplemod.entity.CultistEchoEntity;
import com.example.examplemod.entity.FrostShellSilverfishEntity;
import com.example.examplemod.entity.GoatHunterButcherEntity;
import com.example.examplemod.entity.StarManePegasusEntity;
import com.example.examplemod.entity.TormentedWraithEntity;
import com.example.examplemod.entity.AzureDragonEntity;
import com.example.examplemod.registry.ModEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.STAR_MANE_PEGASUS.get(), StarManePegasusEntity.createAttributes().build());
        event.put(ModEntities.FROST_SHELL_SILVERFISH.get(), FrostShellSilverfishEntity.createAttributes().build());
        event.put(ModEntities.BLACK_MANE_HOUND.get(), BlackManeHoundEntity.createAttributes().build());
        event.put(ModEntities.CULTIST_ECHO.get(), CultistEchoEntity.createAttributes().build());
        event.put(ModEntities.ANCIENT_CULTIST.get(), AncientCultistEntity.createAttributes().build());
        event.put(ModEntities.ALTAR_CULTIST.get(), AltarCultistEntity.createAttributes().build());
        event.put(ModEntities.GOAT_HUNTER_BUTCHER.get(), GoatHunterButcherEntity.createAttributes().build());
        event.put(ModEntities.TORMENTED_WRAITH.get(), TormentedWraithEntity.createAttributes().build());
        event.put(ModEntities.AZURE_DRAGON.get(), AzureDragonEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.STAR_MANE_PEGASUS.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> level.getBlockState(pos.below()).isValidSpawn(level, pos.below(), type),
                SpawnPlacementRegisterEvent.Operation.OR);
        event.register(ModEntities.FROST_SHELL_SILVERFISH.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(ModEntities.BLACK_MANE_HOUND.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
        event.register(ModEntities.TORMENTED_WRAITH.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
    }
}
