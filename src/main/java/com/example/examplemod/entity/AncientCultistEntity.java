package com.example.examplemod.entity;

import com.example.examplemod.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.Vec3;

public class AncientCultistEntity extends Monster {
    private int rangedCooldown;

    public AncientCultistEntity(EntityType<? extends AncientCultistEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D).add(Attributes.ARMOR, 2.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D).add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
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

    protected float rangedDamage() { return 4.0F; }
    protected int rangedDelay() { return 80; }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) return;
        if (rangedCooldown > 0) rangedCooldown--;
        LivingEntity target = getTarget();
        if (rangedCooldown == 0 && target != null && target.isAlive() && hasLineOfSight(target)
                && distanceToSqr(target) > 16.0D && distanceToSqr(target) < 256.0D) {
            LightWaveProjectileEntity projectile = new LightWaveProjectileEntity(ModEntities.LIGHT_WAVE_PROJECTILE.get(), level());
            projectile.setOwner(this);
            projectile.setPos(getX(), getEyeY() - 0.15D, getZ());
            projectile.setSkillValues(rangedDamage(), 18.0D, 0);
            Vec3 delta = target.getEyePosition().subtract(projectile.position());
            projectile.shoot(delta.x, delta.y, delta.z, 1.1F, 1.5F);
            ((ServerLevel) level()).addFreshEntity(projectile);
            ((ServerLevel) level()).sendParticles(ParticleTypes.ENCHANT, getX(), getEyeY(), getZ(), 8, 0.2D, 0.2D, 0.2D, 0.02D);
            rangedCooldown = rangedDelay();
        }
    }
}
