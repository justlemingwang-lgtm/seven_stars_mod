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

public class TriangleShardProjectileEntity extends ThrowableItemProjectile {
    private float damage = 2.5F;
    private double maxRange = 14.0D;
    private int drainQi = 3;
    private double startX;
    private double startY;
    private double startZ;

    public TriangleShardProjectileEntity(EntityType<? extends TriangleShardProjectileEntity> type, Level level) {
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
        return ModItems.TRIANGLE_FRAGMENT.get();
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
            serverLevel.sendParticles(ParticleTypes.PORTAL, getX(), getY(), getZ(), 8, 0.12D, 0.12D, 0.12D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 4, 0.08D, 0.08D, 0.08D, 0.02D);
        }
    }
}
