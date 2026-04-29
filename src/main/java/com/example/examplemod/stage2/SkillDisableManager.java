package com.example.examplemod.stage2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SkillDisableManager {
    private static final String DISABLED_SKILLS_TAG = "sevenstars_disabled_skills";
    private static final String SKILL_COOLDOWNS_TAG = "sevenstars_skill_cooldowns";

    private SkillDisableManager() {
    }

    public static void disableSkill(Player player, String skillId, int ticks) {
        if (player.level().isClientSide() || ticks <= 0) {
            return;
        }
        disabledSkills(player).putInt(skillId, ticks);
        player.displayClientMessage(Component.translatable("message.sevenstars.skill_disabled", skillDisplayName(skillId), ticks / 20), true);
        // TODO sync this map to clients if/when a disabled-skill HUD is added.
    }

    public static boolean isSkillDisabled(Player player, String skillId) {
        return getDisabledTicks(player, skillId) > 0;
    }

    public static int getDisabledTicks(Player player, String skillId) {
        return disabledSkills(player).getInt(skillId);
    }

    public static void clearDisabledSkill(Player player, String skillId) {
        if (!player.level().isClientSide()) {
            disabledSkills(player).remove(skillId);
        }
    }

    public static void tickDisabledSkills(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        tickMap(disabledSkills(player));
    }

    public static void setCooldown(Player player, String skillId, int ticks) {
        if (!player.level().isClientSide() && ticks > 0) {
            cooldowns(player).putInt(skillId, ticks);
        }
    }

    public static boolean isOnCooldown(Player player, String skillId) {
        return getCooldownTicks(player, skillId) > 0;
    }

    public static int getCooldownTicks(Player player, String skillId) {
        return cooldowns(player).getInt(skillId);
    }

    public static void tickCooldowns(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        tickMap(cooldowns(player));
    }

    public static Map<String, Integer> getDisabledSkillTicks(Player player) {
        return copyIntMap(disabledSkills(player));
    }

    public static Map<String, Integer> getCooldownTicks(Player player) {
        return copyIntMap(cooldowns(player));
    }

    private static CompoundTag disabledSkills(Player player) {
        return childTag(player, DISABLED_SKILLS_TAG);
    }

    private static CompoundTag cooldowns(Player player) {
        return childTag(player, SKILL_COOLDOWNS_TAG);
    }

    private static CompoundTag childTag(Player player, String key) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(key, 10)) {
            root.put(key, new CompoundTag());
        }
        return root.getCompound(key);
    }

    private static void tickMap(CompoundTag tag) {
        List<String> keys = new ArrayList<>(tag.getAllKeys());
        for (String key : keys) {
            int remaining = tag.getInt(key) - 1;
            if (remaining <= 0) {
                tag.remove(key);
            } else {
                tag.putInt(key, remaining);
            }
        }
    }

    private static Map<String, Integer> copyIntMap(CompoundTag tag) {
        Map<String, Integer> copy = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            int value = tag.getInt(key);
            if (value > 0) {
                copy.put(key, value);
            }
        }
        return copy;
    }

    private static Component skillDisplayName(String skillId) {
        return switch (skillId) {
            case SkillIds.GOAT_HORN -> Component.translatable("skill.sevenstars.goat_horn");
            case SkillIds.TRIANGLE -> Component.translatable("skill.sevenstars.triangle");
            case SkillIds.GOAT_HORN_SPIKES -> Component.translatable("skill.sevenstars.goat_horn_spikes");
            case SkillIds.CHOP -> Component.translatable("skill.sevenstars.chop");
            default -> Component.literal(skillId);
        };
    }
}
