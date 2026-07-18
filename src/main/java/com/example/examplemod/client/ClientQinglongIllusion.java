package com.example.examplemod.client;

public final class ClientQinglongIllusion {
    private static boolean active;
    private static int bossEntityId = -1;
    private static long visualTicks;

    private ClientQinglongIllusion() {
    }

    public static void set(boolean enabled, int entityId) {
        active = enabled;
        bossEntityId = enabled ? entityId : -1;
        if (!enabled) visualTicks = 0;
    }

    public static void clear() {
        set(false, -1);
    }

    public static boolean isActive() {
        return active;
    }

    public static int getBossEntityId() {
        return bossEntityId;
    }

    public static long getVisualTicks() {
        return visualTicks;
    }

    public static void tick() {
        if (active) visualTicks++;
    }
}
