package com.example.examplemod.item;

import com.example.examplemod.qi.QiManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class QiHunterCollarItem extends Item {
    public QiHunterCollarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        int qi = QiManager.getQi(target);
        int maxQi = Math.max(1, QiManager.getMaxQi(target));
        player.displayClientMessage(Component.translatable("message.sevenstars.qi_hunter_collar", qi, maxQi), true);
        if (qi / (double) maxQi < 0.30D) {
            player.displayClientMessage(Component.translatable("message.sevenstars.qi_hunter_weak"), true);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResult.SUCCESS;
    }
}
