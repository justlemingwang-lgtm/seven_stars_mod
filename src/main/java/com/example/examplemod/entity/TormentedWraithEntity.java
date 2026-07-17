package com.example.examplemod.entity;

import com.example.examplemod.block.SoulCalmingLampBlock;
import com.example.examplemod.qi.QiManager;
import com.example.examplemod.registry.ModBlocks;
import com.example.examplemod.registry.ModEntities;
import com.example.examplemod.stage2.Stage2Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TormentedWraithEntity extends Monster {
    private static final EntityDataAccessor<Boolean> MANIFESTED = SynchedEntityData.defineId(TormentedWraithEntity.class, EntityDataSerializers.BOOLEAN);
    private int rangedCooldown;

    public TormentedWraithEntity(EntityType<? extends TormentedWraithEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Stage2Constants.WRAITH_HEALTH)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, Stage2Constants.WRAITH_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, Stage2Constants.WRAITH_SPEED)
                .add(Attributes.FOLLOW_RANGE, Stage2Constants.WRAITH_FOLLOW_RANGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MANIFESTED, false);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide() && tickCount == 1) {
            QiManager.setMaxQi(this, Stage2Constants.WRAITH_MAX_QI, true);
        }
        if (!level().isClientSide() && tickCount % Stage2Constants.SOUL_LAMP_CHECK_INTERVAL == 0) {
            updateManifestedState();
        }
        if (!level().isClientSide() && rangedCooldown > 0) {
            rangedCooldown--;
        }
        if (!level().isClientSide() && shouldUseRangedAttack(getTarget())) {
            useRangedAttack(getTarget());
        }
        if (isManifested() && level() instanceof ServerLevel serverLevel && tickCount % 8 == 0) {
            serverLevel.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.0D, getZ(), 2, 0.25D, 0.35D, 0.25D, 0.01D);
        }
    }

    public boolean isManifested() {
        return entityData.get(MANIFESTED);
    }

    public void setManifested(boolean manifested) {
        if (isManifested() == manifested) {
            return;
        }
        entityData.set(MANIFESTED, manifested);
        double speed = manifested ? Stage2Constants.WRAITH_SPEED * Stage2Constants.WRAITH_MANIFESTED_SPEED_MULTIPLIER : Stage2Constants.WRAITH_SPEED;
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    public void updateManifestedState() {
        int radius = Stage2Constants.SOUL_LAMP_RADIUS;
        BlockPos center = blockPosition();
        boolean foundActiveLamp = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (pos.distSqr(center) > radius * radius) {
                continue;
            }
            BlockState state = level().getBlockState(pos);
            if (state.is(ModBlocks.SOUL_CALMING_LAMP.get()) && state.hasProperty(SoulCalmingLampBlock.ACTIVE)
                    && state.getValue(SoulCalmingLampBlock.ACTIVE)) {
                foundActiveLamp = true;
                break;
            }
        }
        setManifested(foundActiveLamp);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Without an active soul-calming lamp nearby, the wraith is fully incorporeal and ignores damage.
        if (!isManifested()) {
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.0D, getZ(), 5, 0.25D, 0.35D, 0.25D, 0.02D);
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    private boolean shouldUseRangedAttack(LivingEntity target) {
        if (rangedCooldown > 0 || target == null || !target.isAlive() || !hasLineOfSight(target)) {
            return false;
        }
        double distanceSqr = distanceToSqr(target);
        return distanceSqr > 2.5D * 2.5D
                && distanceSqr <= Stage2Constants.WRAITH_RANGED_RANGE * Stage2Constants.WRAITH_RANGED_RANGE;
    }

    private void useRangedAttack(LivingEntity target) {
        ServerLevel serverLevel = (ServerLevel) level();
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        FrostMobProjectileEntity projectile = new FrostMobProjectileEntity(ModEntities.FROST_MOB_PROJECTILE.get(), level());
        projectile.setOwner(this);
        projectile.setPos(getX(), getEyeY() - 0.1D, getZ());
        projectile.setValues(Stage2Constants.WRAITH_RANGED_DAMAGE, Stage2Constants.WRAITH_RANGED_RANGE,
                Stage2Constants.WRAITH_RANGED_DRAIN_QI, Stage2Constants.WRAITH_RANGED_SLOW_TICKS);
        Vec3 delta = target.getEyePosition().subtract(projectile.position());
        projectile.shoot(delta.x, delta.y, delta.z, Stage2Constants.WRAITH_RANGED_SPEED, 0.35F);
        serverLevel.addFreshEntity(projectile);
        serverLevel.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.0D, getZ(), 8, 0.2D, 0.25D, 0.2D, 0.03D);
        rangedCooldown = Stage2Constants.WRAITH_RANGED_COOLDOWN_MIN_TICKS + random.nextInt(Stage2Constants.WRAITH_RANGED_COOLDOWN_RANDOM_TICKS);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Manifested", isManifested());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setManifested(tag.getBoolean("Manifested"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }
}
