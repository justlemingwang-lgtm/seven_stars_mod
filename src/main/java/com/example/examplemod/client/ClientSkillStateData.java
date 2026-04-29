package com.example.examplemod.client;

import java.util.HashMap;
import java.util.Map;

public final class ClientSkillStateData {
    private static final Map<String, Integer> COOLDOWNS = new HashMap<>();
    private static final Map<String, Integer> DISABLED = new HashMap<>();

    private ClientSkillStateData() {
    }

    public static void set(Map<String, Integer> cooldowns, Map<String, Integer> disabled) {
        COOLDOWNS.clear();
        COOLDOWNS.putAll(cooldowns);
        DISABLED.clear();
        DISABLED.putAll(disabled);
    }

    public static int getCooldownTicks(String skillId) {
        return COOLDOWNS.getOrDefault(skillId, 0);
    }

    public static int getDisabledTicks(String skillId) {
        return DISABLED.getOrDefault(skillId, 0);
    }
}
