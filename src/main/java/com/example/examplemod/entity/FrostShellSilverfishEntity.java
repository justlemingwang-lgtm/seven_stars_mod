package com.example.examplemod.entity;

import com.example.examplemod.qi.QiManager;
import com.example.examplemod.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FrostShellSilverfishEntity extends Monster {
    private long nextFrostShotTime;

    public FrostShellSilverfishEntity(EntityType<? extends FrostShellSilverfishEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 18.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = getTarget();
        if (target != null && target.isAlive() && distanceTo(target) <= 8.0F && tickCount >= nextFrostShotTime && QiManager.consumeQi(this, 15)) {
            nextFrostShotTime = tickCount + 80;
            FrostMobProjectileEntity projectile = new FrostMobProjectileEntity(ModEntities.FROST_MOB_PROJECTILE.get(), level());
            projectile.setOwner(this);
            projectile.setPos(getX(), getEyeY() - 0.1D, getZ());
            projectile.setValues(3.0F, 8.0D, 6, 30);
            double dx = target.getX() - getX();
            double dy = target.getEyeY() - projectile.getY();
            double dz = target.getZ() - getZ();
            projectile.shoot(dx, dy, dz, 0.8F, 0.4F);
            level().addFreshEntity(projectile);
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            amount *= 1.5F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.getEntitiesOfClass(Player.class, getBoundingBox().inflate(3.0D))
                    .forEach(player -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0)));
        }
    }
}
