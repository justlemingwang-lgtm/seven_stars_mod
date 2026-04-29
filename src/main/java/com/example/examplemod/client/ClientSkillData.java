package com.example.examplemod.client;

import com.example.examplemod.skill.SkillSeries;

import java.util.HashSet;
import java.util.Set;

public class ClientSkillData {
    private static final Set<String> UNLOCKED = new HashSet<>();
    private static final Set<SkillSeries> UNLOCKED_SERIES = new HashSet<>();

    public static boolean isUnlocked(String skillId) {
        return UNLOCKED.contains(skillId);
    }

    public static boolean isSeriesUnlocked(SkillSeries series) {
        return series == SkillSeries.BASIC || UNLOCKED_SERIES.contains(series);
    }

    public static Set<String> getUnlockedSkills() {
        return Set.copyOf(UNLOCKED);
    }

    public static void setUnlocked(Set<String> skills, Set<SkillSeries> series) {
        UNLOCKED.clear();
        UNLOCKED.addAll(skills);
        UNLOCKED_SERIES.clear();
        UNLOCKED_SERIES.addAll(series);
    }
}
