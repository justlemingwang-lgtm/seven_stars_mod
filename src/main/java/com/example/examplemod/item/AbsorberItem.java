package com.example.examplemod.item;

import com.example.examplemod.Config;
import com.example.examplemod.qi.QiManager;
import com.example.examplemod.stage2.QiDamageRules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class AbsorberItem extends Item {
    public AbsorberItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(net.minecraft.world.item.ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        int drained = 0;
        if (!(target instanceof Player targetPlayer) || !QiDamageRules.blocksRegularQiDrain(targetPlayer)) {
            drained = QiManager.drainQi(target, Config.getToolDrainAmountInt());
        }
        player.displayClientMessage(Component.translatable("message.sevenstars.absorber.success",
                drained, QiManager.getQi(target), QiManager.getMaxQi(target)), true);
        return InteractionResult.CONSUME;
    }
}
