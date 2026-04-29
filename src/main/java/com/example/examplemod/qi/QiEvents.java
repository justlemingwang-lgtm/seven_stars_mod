package com.example.examplemod.qi;

import com.example.examplemod.Config;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class QiEvents {
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(QiProvider.ID, new QiProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(QiProvider.QI_CAPABILITY).ifPresent(oldData ->
                event.getEntity().getCapability(QiProvider.QI_CAPABILITY).ifPresent(newData ->
                        newData.deserializeNBT(oldData.serializeNBT())));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            QiManager.syncSelf(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            QiManager.syncSelf(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            QiManager.syncSelf(player);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            QiManager.syncSelf(player);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !entity.isAlive() || entity.tickCount % 20 != 0) {
            return;
        }

        int currentQi = QiManager.getQi(entity);
        int maxQi = QiManager.getMaxQi(entity);
        double regenPercent = getRegenPercent(entity);
        if (currentQi >= maxQi || regenPercent <= 0.0D) {
            return;
        }

        int regenAmount = Math.max(1, (int) Math.floor(maxQi * regenPercent));
        QiManager.addQi(entity, regenAmount);
    }

    private static double getRegenPercent(LivingEntity entity) {
        if (entity.hasEffect(ModEffects.QI_EXHAUSTION.get())) {
            return Config.qiExhaustionPercentPerSecond;
        }
        MobEffectInstance surge = entity.getEffect(ModEffects.QI_SURGE.get());
        if (surge != null) {
            return surge.getAmplifier() >= 1 ? Config.qiSurgeTwoPercentPerSecond : Config.qiSurgeOnePercentPerSecond;
        }
        return Config.qiRegenBasePercentPerSecond;
    }

    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IQiData.class);
        }
    }
}
