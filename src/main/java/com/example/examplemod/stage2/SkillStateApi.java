package com.example.examplemod.stage2;

import net.minecraft.world.entity.player.Player;

public final class SkillStateApi {
    private SkillStateApi() {
    }

    public static int getSkillCooldownTicks(Player player, String skillId) {
        return SkillDisableManager.getCooldownTicks(player, skillId);
    }

    public static boolean isSkillOnCooldown(Player player, String skillId) {
        return SkillDisableManager.isOnCooldown(player, skillId);
    }

    public static int getSkillDisabledTicks(Player player, String skillId) {
        return SkillDisableManager.getDisabledTicks(player, skillId);
    }

    public static boolean isSkillDisabled(Player player, String skillId) {
        return SkillDisableManager.isSkillDisabled(player, skillId);
    }

    public static boolean canUseSkill(Player player, String skillId) {
        if (SkillIds.GOAT_HORN_SPIKES.equals(skillId) && isSkillDisabled(player, SkillIds.GOAT_HORN)) {
            return false;
        }
        if (SkillIds.SEVEN_SCATTERED_STRIKES.equals(skillId) && isSkillDisabled(player, SkillIds.TRIANGLE)) {
            return false;
        }
        return !isSkillDisabled(player, skillId) && !isSkillOnCooldown(player, skillId);
    }
}
