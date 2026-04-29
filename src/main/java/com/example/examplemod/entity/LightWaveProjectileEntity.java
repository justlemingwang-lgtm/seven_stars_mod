package com.example.examplemod.entity;

import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.SkillManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LightWaveProjectileEntity extends ThrowableItemProjectile {
    private float damage = 4.0F;
    private double maxRange = 12.0D;
    private double startX;
    private double startY;
    private double startZ;

    public LightWaveProjectileEntity(EntityType<? extends LightWaveProjectileEntity> type, Level level) {
        super(type, level);
    }

    public void setSkillValues(float damage, double maxRange, int ignoredDrain) {
        this.damage = damage;
        this.maxRange = maxRange;
        this.startX = getX();
        this.startY = getY();
        this.startZ = getZ();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SPELL_FRAGMENT.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target && result.getEntity() != getOwner()) {
            if (getOwner() instanceof ServerPlayer caster) {
                SkillManager.applySkillDamage(caster, target, damage);
            } else {
                target.hurt(damageSources().magic(), damage);
            }
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide() && result.getType() == HitResult.Type.BLOCK) {
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
}
