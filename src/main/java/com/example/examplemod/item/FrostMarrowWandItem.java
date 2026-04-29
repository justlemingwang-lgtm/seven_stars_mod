package com.example.examplemod.item;

import com.example.examplemod.entity.FrostMobProjectileEntity;
import com.example.examplemod.qi.QiManager;
import com.example.examplemod.registry.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

public class FrostMarrowWandItem extends SwordItem {
    public FrostMarrowWandItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (!QiManager.consumeQi(player, 12)) {
                serverPlayer.displayClientMessage(Component.translatable("message.sevenstars.not_enough_qi"), true);
                return InteractionResultHolder.fail(stack);
            }
            FrostMobProjectileEntity projectile = new FrostMobProjectileEntity(ModEntities.FROST_MOB_PROJECTILE.get(), level);
            projectile.setOwner(player);
            projectile.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
            projectile.setValues(2.0F, 8.0D, 0, 20);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.8F, 0.25F);
            ((ServerLevel) level).addFreshEntity(projectile);
            serverPlayer.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
