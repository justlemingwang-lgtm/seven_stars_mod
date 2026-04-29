package com.example.examplemod.item;

import com.example.examplemod.skill.AdvancedSkillStateManager;
import com.example.examplemod.skill.PlayerSkillProvider;
import com.example.examplemod.skill.SkillSeries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IceDipperGaugeItem extends Item {
    public IceDipperGaugeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            player.getCapability(PlayerSkillProvider.SKILL_CAPABILITY).ifPresent(data -> {
                if (!data.isSeriesUnlocked(SkillSeries.ICE_DIPPER)) {
                    serverPlayer.displayClientMessage(Component.translatable("message.sevenstars.ice_dipper_locked"), true);
                } else {
                    int percent = (int) Math.round(AdvancedSkillStateManager.getIceChargeRatio(serverPlayer) * 100.0D);
                    serverPlayer.displayClientMessage(Component.translatable("message.sevenstars.ice_dipper_charge", percent), true);
                }
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
