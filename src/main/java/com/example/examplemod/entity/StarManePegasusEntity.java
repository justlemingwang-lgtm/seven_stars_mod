package com.example.examplemod.entity;

import com.example.examplemod.qi.QiManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class StarManePegasusEntity extends PathfinderMob {
    private long nextStepTime;
    private long pacifiedUntil;

    public StarManePegasusEntity(EntityType<? extends StarManePegasusEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 28.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.FOLLOW_RANGE, 18.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.35D));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = getLastHurtByMob();
        if (target != null && target.isAlive() && tickCount >= nextStepTime && level().getGameTime() > pacifiedUntil) {
            if (QiManager.consumeQi(this, 20)) {
                nextStepTime = tickCount + 120;
                Vec3 away = position().subtract(target.position()).normalize();
                if (away.lengthSqr() < 0.001D) {
                    away = getLookAngle();
                }
                Vec3 end = position().add(away.scale(4.0D + random.nextDouble() * 2.0D));
                teleportTo(end.x, end.y, end.z);
                if (distanceTo(target) < 2.4F) {
                    target.hurt(damageSources().mobAttack(this), 4.0F);
                    QiManager.drainQi(target, 8);
                    target.knockback(0.35D, getX() - target.getX(), getZ() - target.getZ());
                }
            }
        }
    }

    public void pacify(int ticks) {
        pacifiedUntil = level().getGameTime() + ticks;
        nextStepTime = Math.max(nextStepTime, tickCount + ticks);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return super.mobInteract(player, hand);
    }

    public Component getDisplayName() {
        return Component.translatable("entity.sevenstars.star_mane_pegasus");
    }
}
