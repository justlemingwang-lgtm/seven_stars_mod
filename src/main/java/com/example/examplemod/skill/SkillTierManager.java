package com.example.examplemod.skill;

import java.util.Set;

public class SkillTierManager {
    private static final Set<String> BASIC_REQUIRED = Set.of(
            SkillRegistry.LIGHT_WAVE,
            SkillRegistry.SEVEN_STAR,
            SkillRegistry.SIX_MERIDIAN_SWORD,
            SkillRegistry.GOLDEN_FINGER
    );
    private static final Set<SkillSeries> TIER_ONE_REQUIRED_SERIES = Set.of(
            SkillSeries.PEGASUS,
            SkillSeries.ICE_DIPPER,
            SkillSeries.HOUND_CLAW
    );

    public static boolean canAccessTier(Set<String> unlockedSkills, int tier) {
        if (tier <= 0) {
            return true;
        }
        if (tier == 1) {
            return unlockedSkills.containsAll(BASIC_REQUIRED);
        }
        if (tier == 2) {
            return unlockedSkills.containsAll(BASIC_REQUIRED);
        }
        return false;
    }

    public static boolean canAccessTier(PlayerSkillData data, int tier) {
        if (tier <= 1) {
            return canAccessTier(data.getUnlockedSkills(), tier);
        }
        if (tier == 2) {
            return data.getUnlockedSkills().containsAll(BASIC_REQUIRED)
                    && TIER_ONE_REQUIRED_SERIES.stream().allMatch(data::isSeriesUnlocked);
        }
        return false;
    }
}
