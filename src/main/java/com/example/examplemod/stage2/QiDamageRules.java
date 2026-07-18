package com.example.examplemod.stage2;

import com.example.examplemod.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import com.example.examplemod.stage3.ArmorDisableManager;

public final class QiDamageRules {
    private QiDamageRules() {
    }

    public static boolean hasActiveGoatHorn(Player player) {
        return !player.isCreative()
                && !player.isSpectator()
                && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GOAT_HORN_ARMOR.get())
                && !ArmorDisableManager.isArmorDisabled(player)
                && !SkillDisableManager.isSkillDisabled(player, SkillIds.GOAT_HORN);
    }

    public static boolean blocksRegularQiLoss(Player player, DamageSource source) {
        // 羊角只抵消常规气损，不抵消生命伤害；技能是否绕过后续接入 DamageSource tag。
        return hasActiveGoatHorn(player) && !bypassesGoatHorn(source);
    }

    public static boolean blocksRegularQiDrain(Player player) {
        return hasActiveGoatHorn(player);
    }

    public static boolean bypassesGoatHorn(DamageSource source) {
        // TODO wire sevenstars:bypass_goat_horn / regular_qi_loss / armor_breaking_skill damage tags.
        return false;
    }
}
