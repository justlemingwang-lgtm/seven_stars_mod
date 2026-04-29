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
    private UUID echoUuid;

    public GoatHornAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GOAT_HORN_ALTAR.get(), pos, state);
    }

    public boolean canSummonEcho() {
        return !echoSummoned && !echoDefeated;
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
        if (echoUuid != null) {
            tag.putUUID("echo_uuid", echoUuid);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        echoSummoned = tag.getBoolean("echo_summoned");
        echoDefeated = tag.getBoolean("echo_defeated");
        echoUuid = tag.hasUUID("echo_uuid") ? tag.getUUID("echo_uuid") : null;
    }
}
