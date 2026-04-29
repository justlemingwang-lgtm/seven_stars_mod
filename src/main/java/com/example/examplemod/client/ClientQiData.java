package com.example.examplemod.client;

public class ClientQiData {
    private static int selfQi;
    private static int selfMaxQi = 100;
    private static int targetEntityId = -1;
    private static int targetQi;
    private static int targetMaxQi = 100;
    private static long targetUpdatedAt;

    public static int getSelfQi() {
        return selfQi;
    }

    public static int getSelfMaxQi() {
        return selfMaxQi;
    }

    public static void setSelf(int qi, int maxQi) {
        selfQi = qi;
        selfMaxQi = maxQi;
    }

    public static void setTarget(int entityId, int qi, int maxQi) {
        targetEntityId = entityId;
        targetQi = qi;
        targetMaxQi = maxQi;
        targetUpdatedAt = System.currentTimeMillis();
    }

    public static void clearTarget() {
        targetEntityId = -1;
    }

    public static boolean hasTarget(int entityId) {
        return targetEntityId == entityId && System.currentTimeMillis() - targetUpdatedAt < 1500L;
    }

    public static int getTargetQi() {
        return targetQi;
    }

    public static int getTargetMaxQi() {
        return targetMaxQi;
    }
}
