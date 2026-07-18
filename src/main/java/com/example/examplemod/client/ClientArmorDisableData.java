package com.example.examplemod.client;

public final class ClientArmorDisableData {
    private static int remainingTicks;

    private ClientArmorDisableData() {
    }

    public static int getRemainingTicks() {
        return remainingTicks;
    }

    public static void setRemainingTicks(int ticks) {
        remainingTicks = Math.max(0, ticks);
    }

    public static void tick() {
        if (remainingTicks > 0) remainingTicks--;
    }
}
