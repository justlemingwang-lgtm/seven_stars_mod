package com.example.examplemod.skill;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerSkillData {
    private final Set<String> unlockedSkills = new HashSet<>();
    private final Set<SkillSeries> unlockedSeries = new HashSet<>();
    private final Map<String, Long> cooldownUntil = new HashMap<>();

    public boolean isUnlocked(String skillId) {
        return unlockedSkills.contains(skillId);
    }

    public Set<String> getUnlockedSkills() {
        return Set.copyOf(unlockedSkills);
    }

    public boolean isSeriesUnlocked(SkillSeries series) {
        return series == SkillSeries.BASIC || unlockedSeries.contains(series);
    }

    public Set<SkillSeries> getUnlockedSeries() {
        return Set.copyOf(unlockedSeries);
    }

    public void unlock(String skillId) {
        unlockedSkills.add(skillId);
    }

    public void unlockSeries(SkillSeries series) {
        if (series != SkillSeries.BASIC) {
            unlockedSeries.add(series);
        }
    }

    public boolean isOnCooldown(String skillId, long gameTime) {
        return cooldownUntil.getOrDefault(skillId, 0L) > gameTime;
    }

    public void setCooldown(String skillId, long untilGameTime) {
        cooldownUntil.put(skillId, untilGameTime);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag unlocked = new ListTag();
        unlockedSkills.forEach(skillId -> unlocked.add(StringTag.valueOf(skillId)));
        tag.put("unlockedSkills", unlocked);

        ListTag series = new ListTag();
        unlockedSeries.forEach(skillSeries -> series.add(StringTag.valueOf(skillSeries.name())));
        tag.put("unlockedSeries", series);

        CompoundTag cooldowns = new CompoundTag();
        cooldownUntil.forEach(cooldowns::putLong);
        tag.put("cooldowns", cooldowns);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        unlockedSkills.clear();
        unlockedSeries.clear();
        cooldownUntil.clear();

        ListTag unlocked = tag.getList("unlockedSkills", Tag.TAG_STRING);
        for (int i = 0; i < unlocked.size(); i++) {
            unlockedSkills.add(unlocked.getString(i));
        }

        ListTag series = tag.getList("unlockedSeries", Tag.TAG_STRING);
        for (int i = 0; i < series.size(); i++) {
            try {
                unlockedSeries.add(SkillSeries.valueOf(series.getString(i)));
            } catch (IllegalArgumentException ignored) {
            }
        }

        CompoundTag cooldowns = tag.getCompound("cooldowns");
        for (String key : cooldowns.getAllKeys()) {
            cooldownUntil.put(key, cooldowns.getLong(key));
        }
    }
}
