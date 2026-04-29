package com.example.examplemod.skill;

import com.example.examplemod.Config;
import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class AdvancedSkillStateManager {
    private static final Map<UUID, Integer> ICE_CHARGE_TICKS = new HashMap<>();
    private static final Map<UUID, Long> FALL_PROTECTION_UNTIL = new HashMap<>();
    private static final Map<UUID, HoundMark> HOUND_MARKS = new HashMap<>();

    public static double getIceChargeRatio(ServerPlayer player) {
        return Math.min(1.0D, ICE_CHARGE_TICKS.getOrDefault(player.getUUID(), Config.iceDipperChargeTicks) / (double) Config.iceDipperChargeTicks);
    }

    public static void resetIceCharge(ServerPlayer player) {
        ICE_CHARGE_TICKS.put(player.getUUID(), 0);
    }

    public static void addFallProtection(ServerPlayer player, int ticks) {
        FALL_PROTECTION_UNTIL.put(player.getUUID(), player.serverLevel().getGameTime() + ticks);
    }

    public static void markHoundTarget(ServerPlayer caster, LivingEntity target, int ticks) {
        HOUND_MARKS.put(target.getUUID(), new HoundMark(caster.getUUID(), target.level().getGameTime() + ticks));
    }

    public static float applyHoundMarkBonus(ServerPlayer caster, LivingEntity target, float damage) {
        HoundMark mark = HOUND_MARKS.get(target.getUUID());
        if (mark == null || mark.expiresAt < target.level().getGameTime()) {
            HOUND_MARKS.remove(target.getUUID());
            return damage;
        }
        if (!mark.caster.equals(caster.getUUID())) {
            return damage;
        }
        HOUND_MARKS.remove(target.getUUID());
        return (float) (damage * (1.0D + Config.houndMarkDamageBonus));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ICE_CHARGE_TICKS.replaceAll((uuid, ticks) -> Math.min(Config.iceDipperChargeTicks, ticks + 1));
        Iterator<Map.Entry<UUID, HoundMark>> iterator = HOUND_MARKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, HoundMark> entry = iterator.next();
            if (entry.getValue().expiresAt <= event.getServer().overworld().getGameTime()) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Long until = FALL_PROTECTION_UNTIL.get(player.getUUID());
        if (until != null && until >= player.serverLevel().getGameTime()) {
            event.setCanceled(true);
            player.fallDistance = 0.0F;
        }
    }

    private record HoundMark(UUID caster, long expiresAt) {
    }
}
