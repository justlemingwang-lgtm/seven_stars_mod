package com.example.examplemod.item;

import com.example.examplemod.codex.CodexProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class AzureDragonScaleItem extends Item {
    public AzureDragonScaleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            boolean unlocked = CodexProgress.unlock(serverPlayer, CodexProgress.AZURE_DRAGON);
            serverPlayer.displayClientMessage(Component.translatable(unlocked
                    ? "message.sevenstars.codex.azure_dragon_unlocked"
                    : "message.sevenstars.codex.chapter_already_unlocked"), true);
            CodexProgress.sync(serverPlayer, false);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
