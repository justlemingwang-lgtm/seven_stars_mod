package com.example.examplemod.item;

import com.example.examplemod.qi.QiManager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class StarHoofSpearItem extends SwordItem {
    public StarHoofSpearItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public boolean hurtEnemy(net.minecraft.world.item.ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof Player player && !player.getCooldowns().isOnCooldown(this)
                && QiManager.consumeQi(player, 10)) {
            target.hurt(attacker.damageSources().playerAttack(player), 1.0F);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 0));
            player.getCooldowns().addCooldown(this, 60);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
