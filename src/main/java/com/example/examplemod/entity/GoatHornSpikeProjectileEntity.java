package com.example.examplemod.entity;

import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.SkillManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class GoatHornSpikeProjectileEntity extends ThrowableItemProjectile {
    private float damage = 4.0F;
    private double maxRange = 18.0D;
    private int drainQi = 8;
    private double startX;
    private double startY;
    private double startZ;

    public GoatHornSpikeProjectileEntity(EntityType<? extends GoatHornSpikeProjectileEntity> type, Level level) {
        super(type, level);
    }

    public void setSkillValues(float damage, double maxRange, int drainQi) {
        this.damage = damage;
        this.maxRange = maxRange;
        this.drainQi = drainQi;
        this.startX = getX();
        this.startY = getY();
        this.startZ = getZ();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.COMPLETE_GOAT_HORN.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target && target != getOwner()) {
            if (getOwner() instanceof ServerPlayer caster) {
                SkillManager.applySkillDamage(caster, target, damage);
                SkillManager.applySkillQiDrain(caster, target, drainQi);
            } else {
                target.hurt(damageSources().magic(), damage);
            }
            spawnHitParticles();
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide() && result.getType() == HitResult.Type.BLOCK) {
            spawnHitParticles();
            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && distanceToSqr(startX, startY, startZ) > maxRange * maxRange) {
            discard();
        }
    }

    private void spawnHitParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 8, 0.15D, 0.15D, 0.15D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 3, 0.08D, 0.08D, 0.08D, 0.01D);
        }
    }
}
