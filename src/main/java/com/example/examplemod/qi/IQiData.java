package com.example.examplemod.qi;

import net.minecraft.nbt.CompoundTag;

public interface IQiData {
    int DEFAULT_MAX_QI = 100;

    int getQi();

    int getMaxQi();

    void setQi(int value);

    void setMaxQi(int value);

    int addQi(int amount);

    boolean consumeQi(int amount);

    int drainQi(int amount);

    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag tag);
}
