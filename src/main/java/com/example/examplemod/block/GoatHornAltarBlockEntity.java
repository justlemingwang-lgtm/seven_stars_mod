package com.example.examplemod.block;

import com.example.examplemod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class GoatHornAltarBlockEntity extends BlockEntity {
    private boolean echoSummoned;
    private boolean echoDefeated;
    private boolean altarUnlocked;
    private boolean hornClaimed;
    private UUID echoUuid;

    public GoatHornAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GOAT_HORN_ALTAR.get(), pos, state);
    }

    public boolean canSummonEcho() {
        return altarUnlocked && hornClaimed && !echoSummoned && !echoDefeated;
    }

    public boolean isAltarUnlocked() { return altarUnlocked; }
    public boolean isHornClaimed() { return hornClaimed; }

    public void debugUnlock() {
        altarUnlocked = true;
        setChanged();
    }

    public void markHornClaimed() {
        hornClaimed = true;
        setChanged();
    }

    public void markEchoSummoned(UUID uuid) {
        this.echoSummoned = true;
        this.echoUuid = uuid;
        setChanged();
    }

    public void markEchoDefeated() {
        this.echoDefeated = true;
        this.echoSummoned = false;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("echo_summoned", echoSummoned);
        tag.putBoolean("echo_defeated", echoDefeated);
        tag.putBoolean("altar_unlocked", altarUnlocked);
        tag.putBoolean("horn_claimed", hornClaimed);
        if (echoUuid != null) {
            tag.putUUID("echo_uuid", echoUuid);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        echoSummoned = tag.getBoolean("echo_summoned");
        echoDefeated = tag.getBoolean("echo_defeated");
        altarUnlocked = tag.getBoolean("altar_unlocked");
        hornClaimed = tag.getBoolean("horn_claimed");
        echoUuid = tag.hasUUID("echo_uuid") ? tag.getUUID("echo_uuid") : null;
    }
}
