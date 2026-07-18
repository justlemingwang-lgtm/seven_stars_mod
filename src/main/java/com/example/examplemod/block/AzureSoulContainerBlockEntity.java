package com.example.examplemod.block;

import com.example.examplemod.entity.AzureDragonEntity;
import com.example.examplemod.registry.ModBlockEntities;
import com.example.examplemod.registry.ModBlocks;
import com.example.examplemod.registry.ModEntities;
import com.example.examplemod.stage3.Stage3Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AzureSoulContainerBlockEntity extends BlockEntity {
    private boolean summoned;
    private boolean summonSequenceActive;
    private int summonSequenceTicks;
    private int retryTicks;

    public AzureSoulContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AZURE_SOUL_CONTAINER.get(), pos, state);
    }

    public void onSealChanged() {
        if (summoned || summonSequenceActive) return;
        SealCount count = countSeals();
        if (count.total() == 7 && count.broken() == 7) {
            summonSequenceActive = true;
            summonSequenceTicks = 0;
            retryTicks = 0;
            setChanged();
            syncVisualState();
        }
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel) || summoned) return;
        if (!summonSequenceActive) return;
        summonSequenceTicks++;
        if (summonSequenceTicks % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, 10,
                    1.2D, 1.0D, 1.2D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, 6,
                    0.7D, 0.8D, 0.7D, 0.03D);
        }
        if (summonSequenceTicks < Stage3Constants.SUMMON_SEQUENCE_TICKS) return;
        if (retryTicks-- > 0) return;
        SealCount count = countSeals();
        if (count.total() != 7 || count.broken() != 7) {
            summonSequenceActive = false;
            setChanged();
            syncVisualState();
            return;
        }
        AzureDragonEntity dragon = new AzureDragonEntity(ModEntities.AZURE_DRAGON.get(), serverLevel);
        dragon.setArenaCenter(worldPosition);
        dragon.moveTo(worldPosition.getX() + 0.5D, worldPosition.getY() + 5.0D,
                worldPosition.getZ() + 0.5D, 0.0F, 0.0F);
        if (serverLevel.addFreshEntity(dragon)) {
            summoned = true;
            summonSequenceActive = false;
            serverLevel.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0F, 0.75F);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 2.0D, worldPosition.getZ() + 0.5D, 8,
                    1.5D, 1.5D, 1.5D, 0.0D);
            setChanged();
            syncVisualState();
        } else {
            retryTicks = Stage3Constants.SOUL_CONTAINER_RETRY_TICKS;
        }
    }

    private SealCount countSeals() {
        if (level == null) return new SealCount(0, 0);
        int total = 0;
        int broken = 0;
        int radius = Stage3Constants.SEAL_SCAN_RADIUS;
        for (BlockPos scan : BlockPos.betweenClosed(worldPosition.offset(-radius, -radius / 2, -radius),
                worldPosition.offset(radius, radius / 2, radius))) {
            BlockState state = level.getBlockState(scan);
            if (state.is(ModBlocks.AZURE_SEAL_CHAIN.get())) {
                total++;
                if (state.getValue(AzureSealChainBlock.BROKEN)) broken++;
            }
        }
        return new SealCount(total, broken);
    }

    public String debugStatus() {
        SealCount count = countSeals();
        return "Azure seals total=" + count.total() + ", broken=" + count.broken()
                + ", summoned=" + summoned + ", sequence=" + summonSequenceActive
                + ", ticks=" + summonSequenceTicks;
    }

    private void syncVisualState() {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.hasProperty(AzureSoulContainerBlock.ACTIVE)) {
            level.setBlock(worldPosition, state.setValue(AzureSoulContainerBlock.ACTIVE, summonSequenceActive)
                    .setValue(AzureSoulContainerBlock.SUMMONED, summoned), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Summoned", summoned);
        tag.putBoolean("SummonSequenceActive", summonSequenceActive);
        tag.putInt("SummonSequenceTicks", summonSequenceTicks);
        tag.putInt("RetryTicks", retryTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        summoned = tag.getBoolean("Summoned");
        summonSequenceActive = tag.getBoolean("SummonSequenceActive");
        summonSequenceTicks = tag.getInt("SummonSequenceTicks");
        retryTicks = tag.getInt("RetryTicks");
    }

    private record SealCount(int total, int broken) {
    }
}
