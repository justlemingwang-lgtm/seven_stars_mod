package com.example.examplemod.skill;

import com.example.examplemod.Config;
import com.example.examplemod.qi.QiManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CastStateManager {
    private static final Map<UUID, CastState> CASTING = new HashMap<>();

    public static boolean isCasting(ServerPlayer player) {
        return CASTING.containsKey(player.getUUID());
    }

    public static void startSevenStar(ServerPlayer player) {
        long now = player.serverLevel().getGameTime();
        CASTING.put(player.getUUID(), new CastState(SkillRegistry.SEVEN_STAR, now + Config.sevenStarCastTicks,
                now + Config.sevenStarInterruptWindowTicks));
        player.displayClientMessage(Component.translatable("message.sevenstars.seven_star_cast_start"), true);
    }

    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CastState state = CASTING.get(player.getUUID());
        if (state != null && player.serverLevel().getGameTime() <= state.interruptUntil()) {
            CASTING.remove(player.getUUID());
            player.displayClientMessage(Component.translatable("message.sevenstars.seven_star_interrupted"), true);
        }
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().overworld() == null) {
            return;
        }
        long now = event.getServer().overworld().getGameTime();
        Iterator<Map.Entry<UUID, CastState>> iterator = CASTING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, CastState> entry = iterator.next();
            CastState state = entry.getValue();
            if (now < state.finishTick()) {
                continue;
            }
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            iterator.remove();
            if (player == null || !player.isAlive()) {
                continue;
            }
            if (SkillRegistry.SEVEN_STAR.equals(state.skillId())) {
                int recover = Math.max(1, (int) Math.floor(QiManager.getMaxQi(player) * Config.sevenStarRecoverPercent));
                QiManager.addQi(player, recover);
                player.displayClientMessage(Component.translatable("message.sevenstars.seven_star_success"), true);
            }
        }
    }

    private record CastState(String skillId, long finishTick, long interruptUntil) {
    }
}
