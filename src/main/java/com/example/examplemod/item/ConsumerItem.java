package com.example.examplemod.item;

import com.example.examplemod.Config;
import com.example.examplemod.qi.QiManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ConsumerItem extends Item {
    public ConsumerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        consumeSelf(player);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        consumeSelf(player);
        return InteractionResult.CONSUME;
    }

    private void consumeSelf(Player player) {
        int amount = Config.getToolConsumeAmountInt();
        if (!QiManager.consumeQi(player, amount)) {
            player.displayClientMessage(Component.translatable("message.sevenstars.consumer.not_enough"), true);
            return;
        }

        player.displayClientMessage(Component.translatable("message.sevenstars.consumer.success",
                amount, QiManager.getQi(player), QiManager.getMaxQi(player)), true);
    }
}
