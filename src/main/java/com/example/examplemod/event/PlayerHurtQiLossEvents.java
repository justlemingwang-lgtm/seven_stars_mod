package com.example.examplemod.event;

import com.example.examplemod.Config;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.qi.QiManager;
import com.example.examplemod.skill.SkillManager;
import com.example.examplemod.stage2.QiDamageRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class PlayerHurtQiLossEvents {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0.0F || SkillManager.isApplyingQiCollapseDamage()
                || player.isCreative() || player.isSpectator()) {
            return;
        }

        if (QiDamageRules.blocksRegularQiLoss(player, event.getSource())) {
            return;
        }

        int min = Math.max(0, Config.playerHurtQiLossMin);
        int max = Math.max(min, Config.playerHurtQiLossMax);
        int amount = min + player.getRandom().nextInt(max - min + 1);
        if (amount <= 0) {
            return;
        }

        if (Config.playerHurtQiLossCanTriggerCollapse) {
            SkillManager.applyCombatQiDrain(player, amount);
        } else {
            QiManager.drainQi(player, amount);
        }
    }
}
