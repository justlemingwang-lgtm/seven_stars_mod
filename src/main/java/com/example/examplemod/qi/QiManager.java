package com.example.examplemod.qi;

import com.example.examplemod.network.ModNetwork;
import com.example.examplemod.network.SyncSelfQiPacket;
import com.example.examplemod.network.SyncTargetQiPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

public class QiManager {
    public static Optional<IQiData> getData(LivingEntity entity) {
        return entity.getCapability(QiProvider.QI_CAPABILITY).resolve();
    }

    public static int getQi(LivingEntity entity) {
        return getData(entity).map(IQiData::getQi).orElse(0);
    }

    public static int getMaxQi(LivingEntity entity) {
        return getData(entity).map(IQiData::getMaxQi).orElse(IQiData.DEFAULT_MAX_QI);
    }

    public static void setQi(LivingEntity entity, int value) {
        if (entity.level().isClientSide()) {
            return;
        }
        getData(entity).ifPresent(data -> {
            data.setQi(value);
            syncChanged(entity);
        });
    }

    public static void setMaxQi(LivingEntity entity, int value, boolean fillToMax) {
        if (entity.level().isClientSide()) {
            return;
        }
        getData(entity).ifPresent(data -> {
            data.setMaxQi(value);
            if (fillToMax) {
                data.setQi(data.getMaxQi());
            }
            syncChanged(entity);
        });
    }

    public static int addQi(LivingEntity entity, int amount) {
        if (entity.level().isClientSide()) {
            return 0;
        }
        return getData(entity).map(data -> {
            int added = data.addQi(amount);
            if (added > 0) {
                syncChanged(entity);
            }
            return added;
        }).orElse(0);
    }

    public static boolean consumeQi(LivingEntity entity, int amount) {
        if (entity.level().isClientSide()) {
            return false;
        }
        return getData(entity).map(data -> {
            boolean consumed = data.consumeQi(amount);
            if (consumed && amount > 0) {
                syncChanged(entity);
            }
            return consumed;
        }).orElse(false);
    }

    public static int drainQi(LivingEntity entity, int amount) {
        if (entity.level().isClientSide()) {
            return 0;
        }
        return getData(entity).map(data -> {
            int drained = data.drainQi(amount);
            if (drained > 0) {
                syncChanged(entity);
            }
            return drained;
        }).orElse(0);
    }

    public static void syncSelf(ServerPlayer player) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncSelfQiPacket(getQi(player), getMaxQi(player)));
    }

    public static void syncTarget(ServerPlayer player, LivingEntity target) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncTargetQiPacket(target.getId(), getQi(target), getMaxQi(target)));
    }

    private static void syncChanged(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            syncSelf(player);
        }
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new SyncTargetQiPacket(entity.getId(), getQi(entity), getMaxQi(entity)));
        if (entity instanceof Player player && player instanceof ServerPlayer serverPlayer) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new SyncTargetQiPacket(entity.getId(), getQi(entity), getMaxQi(entity)));
        }
    }
}
