package com.example.examplemod.event;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.network.ModNetwork;
import com.example.examplemod.network.SyncSkillStatePacket;
import com.example.examplemod.stage2.SkillDisableManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class Stage2PlayerEvents {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide()) {
            return;
        }
        SkillDisableManager.tickDisabledSkills(player);
        SkillDisableManager.tickCooldowns(player);
        if (player instanceof ServerPlayer serverPlayer && player.tickCount % 5 == 0) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new SyncSkillStatePacket(SkillDisableManager.getCooldownTicks(player), SkillDisableManager.getDisabledSkillTicks(player)));
        }
    }
}
