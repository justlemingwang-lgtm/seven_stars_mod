package com.example.examplemod.entity;

import com.example.examplemod.Config;
import com.example.examplemod.registry.ModEffects;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.SkillManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class IceDipperProjectileEntity extends ThrowableItemProjectile {
    private float damage = 3.0F;
    private double maxRange = 14.0D;
    private int drainQi = 8;
    private int slowTicks = 40;
    private double startX;
    private double startY;
    private double startZ;

    public IceDipperProjectileEntity(EntityType<? extends IceDipperProjectileEntity> type, Level level) {
        super(type, level);
    }

    public void setSkillValues(float damage, double maxRange, int drainQi, int slowTicks) {
        this.damage = damage;
        this.maxRange = maxRange;
        this.drainQi = drainQi;
        this.slowTicks = slowTicks;
        this.startX = getX();
        this.startY = getY();
        this.startZ = getZ();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ICE_DIPPER_SHOT_SCROLL.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target && result.getEntity() != getOwner()) {
            if (getOwner() instanceof ServerPlayer caster) {
                SkillManager.applySkillDamage(caster, target, damage);
                SkillManager.applySkillQiDrain(caster, target, drainQi);
            } else {
                target.hurt(damageSources().magic(), damage);
            }
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, 0));
            applyQiExhaustion(target);
            discard();
        }
    }

    private void applyQiExhaustion(LivingEntity target) {
        MobEffectInstance existing = target.getEffect(ModEffects.QI_EXHAUSTION.get());
        if (existing == null || existing.getDuration() < Config.qiExhaustionFromIceTicks) {
            target.addEffect(new MobEffectInstance(ModEffects.QI_EXHAUSTION.get(), Config.qiExhaustionFromIceTicks, 0));
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
