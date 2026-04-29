package com.example.examplemod.item;

import com.example.examplemod.qi.QiManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class QiFoodItem extends Item {
    private final int qiRestore;
    private final boolean grantsSpeed;

    public QiFoodItem(Properties properties, int qiRestore, boolean grantsSpeed) {
        super(properties);
        this.qiRestore = qiRestore;
        this.grantsSpeed = grantsSpeed;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            QiManager.addQi(player, qiRestore);
            if (grantsSpeed) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0));
            }
        }
        return result;
    }
}
