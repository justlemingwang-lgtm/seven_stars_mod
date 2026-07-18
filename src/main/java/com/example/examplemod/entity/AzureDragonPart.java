package com.example.examplemod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.entity.PartEntity;

/** A separately targetable body segment, following the vanilla Ender Dragon part design. */
public final class AzureDragonPart extends PartEntity<AzureDragonEntity> {
    public final String name;
    private final EntityDimensions dimensions;

    public AzureDragonPart(AzureDragonEntity parent, String name, float width, float height) {
        super(parent);
        this.name = name;
        this.dimensions = EntityDimensions.scalable(width, height);
        refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !isInvulnerableTo(source) && getParent().hurt(this, source, amount);
    }

    @Override
    public boolean is(Entity entity) {
        return entity == this || entity == getParent();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return dimensions;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
