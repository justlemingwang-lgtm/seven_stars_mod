package com.example.examplemod.client;

import java.util.Set;

public final class ClientCodexData {
    private static Set<String> unlocked = Set.of();

    private ClientCodexData() {
    }

    public static void setUnlocked(Set<String> chapters) {
        unlocked = Set.copyOf(chapters);
    }

    public static boolean isUnlocked(String chapterId) {
        return unlocked.contains(chapterId);
    }
}
