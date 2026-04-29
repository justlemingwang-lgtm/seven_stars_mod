package com.example.examplemod.qi;

import com.example.examplemod.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class QiData implements IQiData {
    private int currentQi = Config.getMaxQiDefaultInt();
    private int maxQi = Config.getMaxQiDefaultInt();

    @Override
    public int getQi() {
        return currentQi;
    }

    @Override
    public int getMaxQi() {
        return maxQi;
    }

    @Override
    public void setQi(int value) {
        currentQi = Mth.clamp(value, 0, maxQi);
    }

    @Override
    public void setMaxQi(int value) {
        maxQi = Math.max(1, value);
        setQi(currentQi);
    }

    @Override
    public int addQi(int amount) {
        int before = currentQi;
        setQi(currentQi + Math.max(0, amount));
        return currentQi - before;
    }

    @Override
    public boolean consumeQi(int amount) {
        if (amount <= 0) {
            return true;
        }
        if (currentQi < amount) {
            return false;
        }
        setQi(currentQi - amount);
        return true;
    }

    @Override
    public int drainQi(int amount) {
        int before = currentQi;
        setQi(currentQi - Math.max(0, amount));
        return before - currentQi;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("currentQi", currentQi);
        tag.putInt("maxQi", maxQi);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        maxQi = Math.max(1, tag.getInt("maxQi"));
        if (!tag.contains("maxQi")) {
            maxQi = Config.getMaxQiDefaultInt();
        }
        setQi(tag.contains("currentQi") ? tag.getInt("currentQi") : maxQi);
    }
}
