package com.example.examplemod.entity;

import com.example.examplemod.qi.QiManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlackManeHoundEntity extends Monster {
    private static final Map<UUID, Long> SCRATCHED = new HashMap<>();
    private long nextPounceTime;

    public BlackManeHoundEntity(EntityType<? extends BlackManeHoundEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FOLLOW_RANGE, 22.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0D));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        boolean lowQi = QiManager.getQi(target) / (double) Math.max(1, QiManager.getMaxQi(target)) < 0.30D;
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(lowQi ? 0.39D : 0.32D);
        if (distanceTo(target) <= 3.0F && tickCount >= nextPounceTime && QiManager.consumeQi(this, 18)) {
            nextPounceTime = tickCount + 100;
            double damage = 4.0D + (lowQi ? 2.0D : 0.0D);
            int drain = 10 + (lowQi ? 5 : 0);
            target.hurt(damageSources().mobAttack(this), (float) damage + scratchBonus(target));
            QiManager.drainQi(target, drain);
            SCRATCHED.put(target.getUUID(), level().getGameTime() + 80);
            target.knockback(0.25D, getX() - target.getX(), getZ() - target.getZ());
        }
    }

    private float scratchBonus(LivingEntity target) {
        Long until = SCRATCHED.remove(target.getUUID());
        return until != null && until >= level().getGameTime() ? 1.0F : 0.0F;
    }
}
