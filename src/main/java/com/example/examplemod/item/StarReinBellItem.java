package com.example.examplemod.item;

import com.example.examplemod.entity.StarManePegasusEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class StarReinBellItem extends Item {
    public StarReinBellItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(target instanceof StarManePegasusEntity pegasus)) {
            player.displayClientMessage(Component.translatable("message.sevenstars.star_rein_bell_invalid"), true);
            return InteractionResult.FAIL;
        }
        pegasus.pacify(160);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCooldowns().addCooldown(this, 400);
        }
        player.displayClientMessage(Component.translatable("message.sevenstars.star_rein_bell_success"), true);
        return InteractionResult.SUCCESS;
    }
}
