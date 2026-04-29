package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.network.CastSkillPacket;
import com.example.examplemod.network.ModNetwork;
import com.example.examplemod.network.RequestTargetQiPacket;
import com.example.examplemod.skill.SkillRank;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static int requestCooldown;
    private static int lastTargetId = -1;
    private static boolean wasWheelKeyDown;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        HitResult hitResult = minecraft.hitResult;
        if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity living) {
            int entityId = living.getId();
            if (entityId != lastTargetId) {
                lastTargetId = entityId;
                requestCooldown = 0;
            }
            if (requestCooldown-- <= 0) {
                ModNetwork.CHANNEL.sendToServer(new RequestTargetQiPacket(entityId));
                requestCooldown = 10;
            }
        } else {
            lastTargetId = -1;
            ClientQiData.clearTarget();
        }

        boolean wheelKeyDown = SkillKeyMappings.OPEN_SKILL_WHEEL.isDown() && minecraft.screen == null;
        if (wheelKeyDown && !wasWheelKeyDown) {
            ItemStack scroll = ClientSelectedSkillData.findClientScroll();
            if (scroll.isEmpty()) {
                minecraft.player.displayClientMessage(Component.translatable("message.sevenstars.no_scroll"), true);
            } else {
                SkillWheelOverlay.begin();
            }
        }
        if (wheelKeyDown) {
            SkillWheelOverlay.update();
        }
        if (!wheelKeyDown && wasWheelKeyDown) {
            SkillWheelOverlay.finish();
        }
        wasWheelKeyDown = wheelKeyDown;

        while (SkillKeyMappings.CAST_SELECTED_SKILL.consumeClick()) {
            if (SkillWheelOverlay.isActive()) {
                continue;
            }
            int selectedSlot = ClientSelectedSkillData.getSelectedSlot();
            if (selectedSlot < 0) {
                minecraft.player.displayClientMessage(Component.translatable("message.sevenstars.no_selected_spell"), true);
            } else {
                ModNetwork.CHANNEL.sendToServer(new CastSkillPacket(selectedSlot, ClientSelectedSkillData.getSelectedRank()));
            }
        }

        while (SkillKeyMappings.CYCLE_SKILL_RANK.consumeClick()) {
            SkillRank rank = ClientSelectedSkillData.cycleRank();
            minecraft.player.displayClientMessage(Component.translatable("message.sevenstars.current_rank",
                    ClientSelectedSkillData.getRankDisplayName()), true);
        }
    }
}
