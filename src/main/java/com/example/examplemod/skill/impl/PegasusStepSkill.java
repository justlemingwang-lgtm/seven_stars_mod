package com.example.examplemod.skill.impl;

import com.example.examplemod.Config;
import com.example.examplemod.qi.QiManager;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.AdvancedSkillStateManager;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SkillManager;
import com.example.examplemod.skill.SkillRank;
import com.example.examplemod.skill.SkillRegistry;
import com.example.examplemod.skill.SkillSeries;
import com.example.examplemod.skill.SpellScrollRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class PegasusStepSkill implements Skill {
    @Override
    public String id() {
        return SkillRegistry.PEGASUS_STEP;
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars.pegasus_step");
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars.pegasus_step.desc");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModItems.PEGASUS_STEP_SCROLL.get());
    }

    @Override
    public int tier() {
        return 1;
    }

    @Override
    public SkillSeries series() {
        return SkillSeries.PEGASUS;
    }

    @Override
    public SkillRank rank() {
        return SkillRank.NORMAL;
    }

    @Override
    public int qiCost() {
        return Config.pegasusStepQiCost;
    }

    @Override
    public int cooldownTicks() {
        return Math.min(Config.pegasusStepCooldownTicks, 13);
    }

    @Override
    public List<ItemStack> unlockCost() {
        return List.of(SpellScrollRecipes.get(id()).output().copy());
    }

    @Override
    public boolean cast(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.position();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 wantedEnd = start.add(look.scale(Config.pegasusStepDistance));
        HitResult blockHit = level.clip(new ClipContext(player.getEyePosition(), player.getEyePosition().add(look.scale(Config.pegasusStepDistance)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation().subtract(look.scale(0.75D)) : wantedEnd;

        Vec3 pathEnd = end;
        AABB pathBox = player.getBoundingBox().expandTowards(pathEnd.subtract(start)).inflate(1.0D);
        LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, pathBox,
                        entity -> entity != player && entity.isAlive())
                .stream()
                .filter(entity -> isNearPath(start, pathEnd, entity.position()))
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
        if (target != null) {
            end = target.position().subtract(look.scale(1.0D));
        }

        player.teleportTo(end.x, end.y, end.z);
        player.fallDistance = 0.0F;
        AdvancedSkillStateManager.addFallProtection(player, Config.pegasusStepFallProtectionTicks);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Config.pegasusStepSpeedBuffTicks, 0));

        if (target != null) {
            SkillManager.applySkillDamage(player, target, (float) Config.pegasusStepDamage);
            SkillManager.applySkillQiDrain(player, target, Config.pegasusStepDrainQi);
            QiManager.addQi(player, Config.pegasusStepHitRecoverQi);
        }
        return true;
    }

    private boolean isNearPath(Vec3 start, Vec3 end, Vec3 point) {
        Vec3 path = end.subtract(start);
        double lengthSqr = path.lengthSqr();
        if (lengthSqr < 0.0001D) {
            return false;
        }
        double t = Math.max(0.0D, Math.min(1.0D, point.subtract(start).dot(path) / lengthSqr));
        Vec3 closest = start.add(path.scale(t));
        return closest.distanceToSqr(point) <= 1.8D;
    }
}
