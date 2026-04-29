package com.example.examplemod.item;

import com.example.examplemod.Config;
import com.example.examplemod.registry.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class CrackedClawDaggerItem extends SwordItem {
    public CrackedClawDaggerItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public boolean hurtEnemy(net.minecraft.world.item.ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof Player player) {
            MobEffect combo = ModEffects.CRACKED_CLAW_COMBO.get();
            int currentStacks = player.hasEffect(combo) ? player.getEffect(combo).getAmplifier() + 1 : 0;
            int maxStacks = Math.max(1, Config.crackedClawComboMaxStacks);
            int nextStacks = Math.min(maxStacks, currentStacks + 1);
            player.addEffect(new MobEffectInstance(combo, Config.crackedClawComboDurationTicks, nextStacks - 1, false, true, true));
            if (nextStacks == maxStacks && currentStacks < maxStacks) {
                player.displayClientMessage(Component.translatable("message.sevenstars.cracked_claw_combo_max"), true);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
