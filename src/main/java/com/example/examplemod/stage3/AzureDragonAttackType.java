package com.example.examplemod.stage3;

public enum AzureDragonAttackType {
    NONE,
    PHASE_TRANSITION,
    PHYSICAL_BREATH,
    STOMP,
    TURNING_TAIL,
    AZURE_BREATH,
    DIVINE_TAIL,
    BULL_AZURE_SLASH,
    DEFEATED,
    AERIAL_SLAM,
    CHARGE,
    BITE;

    public static AzureDragonAttackType byId(int id) {
        AzureDragonAttackType[] values = values();
        return id >= 0 && id < values.length ? values[id] : NONE;
    }
}
