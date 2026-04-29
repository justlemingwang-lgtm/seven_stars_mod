package com.example.examplemod.event;

import com.example.examplemod.Config;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.registry.ModEffects;
import com.example.examplemod.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class WeaponCombatEvents {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || !player.getMainHandItem().is(ModItems.CRACKED_CLAW_DAGGER.get())) {
            return;
        }
        MobEffectInstance combo = player.getEffect(ModEffects.CRACKED_CLAW_COMBO.get());
        if (combo == null) {
            return;
        }
        int stacks = Math.min(Math.max(1, Config.crackedClawComboMaxStacks), combo.getAmplifier() + 1);
        float multiplier = (float) (1.0D + Config.crackedClawComboDamageBonusPerStack * stacks);
        event.setAmount(event.getAmount() * multiplier);
    }
}
