package com.example.examplemod.event;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.network.ModNetwork;
import com.example.examplemod.network.QinglongIllusionPacket;
import com.example.examplemod.network.SyncArmorDisablePacket;
import com.example.examplemod.stage3.ArmorDisableManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class Stage3PlayerEvents {
    private Stage3PlayerEvents() {
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        int remaining = ArmorDisableManager.getRemainingTicks(event.getOriginal());
        if (remaining > 0 && event.getEntity() instanceof ServerPlayer player) {
            ArmorDisableManager.disableArmor(player, remaining);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncClean(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncClean(player);
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncClean(player);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new QinglongIllusionPacket(false, -1));
        }
    }

    private static void syncClean(ServerPlayer player) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncArmorDisablePacket(ArmorDisableManager.getRemainingTicks(player)));
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new QinglongIllusionPacket(false, -1));
    }
}
