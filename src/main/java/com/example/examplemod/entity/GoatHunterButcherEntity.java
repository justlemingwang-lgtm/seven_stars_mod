package com.example.examplemod.entity;

import com.example.examplemod.stage2.ChopSkillLogic;
import com.example.examplemod.stage2.QiDamageRules;
import com.example.examplemod.stage2.Stage2Constants;
import com.example.examplemod.registry.ModEntities;
import com.example.examplemod.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GoatHunterButcherEntity extends AbstractIllager {
    private int chopWindup;
    private int chopCooldown;
    private int rangedCooldown;
    private LivingEntity chopTarget;

    public GoatHunterButcherEntity(EntityType<? extends GoatHunterButcherEntity> type, Level level) {
        super(type, level);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.BLOODY_CLEAVER.get()));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.15D);
    }

    @Override
    public IllagerArmPose getArmPose() {
        return getTarget() != null || isAggressive() || chopWindup > 0 ? IllagerArmPose.ATTACKING : IllagerArmPose.CROSSED;
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.VINDICATOR_CELEBRATE;
    }

    @Override
    public void applyRaidBuffs(int wave, boolean unused) {
        // Not wired into raids yet; renderer inherits the illager base only for the temporary model.
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
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            return;
        }
        if (chopCooldown > 0) {
            chopCooldown--;
        }
        if (rangedCooldown > 0) {
            rangedCooldown--;
        }
        if (chopWindup > 0) {
            if (!isValidChopTarget(chopTarget, 4.5D)) {
                chopWindup = 0;
                chopTarget = null;
                return;
            }
            getNavigation().stop();
            getLookControl().setLookAt(chopTarget, 30.0F, 30.0F);
            if (chopWindup % 2 == 0) {
                ChopSkillLogic.spawnChopWarning((ServerLevel) level(), this);
            }
            chopWindup--;
            if (chopWindup == 0) {
                getLookControl().setLookAt(chopTarget, 30.0F, 30.0F);
                ChopSkillLogic.performChop(this, Stage2Constants.BUTCHER_CHOP_DAMAGE, Stage2Constants.BUTCHER_CHOP_RANGE,
                        Stage2Constants.CHOP_HALF_ANGLE_DEGREES, Stage2Constants.CHOP_GOAT_HORN_DISABLE_TICKS, Stage2Constants.BUTCHER_CHOP_KNOCKBACK);
                chopCooldown = Stage2Constants.BUTCHER_CHOP_COOLDOWN_MIN_TICKS + random.nextInt(Stage2Constants.BUTCHER_CHOP_COOLDOWN_RANDOM_TICKS);
                chopTarget = null;
            }
            return;
        }
        LivingEntity target = getTarget();
        if (shouldStartChop(target)) {
            chopWindup = Stage2Constants.BUTCHER_CHOP_WINDUP_TICKS;
            chopTarget = target;
            getNavigation().stop();
            ChopSkillLogic.spawnChopWarning((ServerLevel) level(), this);
            return;
        }
        if (shouldUseRangedSkill(target)) {
            useRangedSkill(target);
        }
    }

    private boolean shouldStartChop(LivingEntity target) {
        if (chopCooldown > 0 || !isValidChopTarget(target, Stage2Constants.BUTCHER_CHOP_RANGE)) {
            return false;
        }
        if (target instanceof Player player && QiDamageRules.hasActiveGoatHorn(player)) {
            return true;
        }
        return target instanceof Player;
    }

    private boolean shouldUseRangedSkill(LivingEntity target) {
        if (rangedCooldown > 0 || target == null || !target.isAlive() || !hasLineOfSight(target)) {
            return false;
        }
        double distanceSqr = distanceToSqr(target);
        double minDistance = Stage2Constants.BUTCHER_CHOP_RANGE + 0.75D;
        return distanceSqr >= minDistance * minDistance
                && distanceSqr <= Stage2Constants.BUTCHER_RANGED_RANGE * Stage2Constants.BUTCHER_RANGED_RANGE;
    }

    private void useRangedSkill(LivingEntity target) {
        ServerLevel serverLevel = (ServerLevel) level();
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        FrostMobProjectileEntity projectile = new FrostMobProjectileEntity(ModEntities.FROST_MOB_PROJECTILE.get(), level());
        projectile.setOwner(this);
        projectile.setPos(getX(), getEyeY() - 0.15D, getZ());
        projectile.setValues(Stage2Constants.BUTCHER_RANGED_DAMAGE, Stage2Constants.BUTCHER_RANGED_RANGE,
                Stage2Constants.BUTCHER_RANGED_DRAIN_QI, Stage2Constants.BUTCHER_RANGED_SLOW_TICKS);
        Vec3 delta = target.getEyePosition().subtract(projectile.position());
        projectile.shoot(delta.x, delta.y, delta.z, Stage2Constants.BUTCHER_RANGED_SPEED, 0.6F);
        serverLevel.addFreshEntity(projectile);
        serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY() + 1.1D, getZ(), 10, 0.35D, 0.35D, 0.35D, 0.02D);
        rangedCooldown = Stage2Constants.BUTCHER_RANGED_COOLDOWN_MIN_TICKS + random.nextInt(Stage2Constants.BUTCHER_RANGED_COOLDOWN_RANDOM_TICKS);
    }

    private boolean isValidChopTarget(LivingEntity target, double range) {
        return target != null
                && target.isAlive()
                && hasLineOfSight(target)
                && distanceToSqr(target) <= range * range
                && !ChopSkillLogic.findTargetsInCone(level(), this, range, Stage2Constants.CHOP_HALF_ANGLE_DEGREES).isEmpty();
    }
}
