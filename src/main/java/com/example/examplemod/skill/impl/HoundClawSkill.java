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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class HoundClawSkill implements Skill {
    @Override
    public String id() {
        return SkillRegistry.HOUND_CLAW;
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars.hound_claw");
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars.hound_claw.desc");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModItems.HOUND_CLAW_SCROLL.get());
    }

    @Override
    public int tier() {
        return 1;
    }

    @Override
    public SkillSeries series() {
        return SkillSeries.HOUND_CLAW;
    }

    @Override
    public SkillRank rank() {
        return SkillRank.NORMAL;
    }

    @Override
    public int qiCost() {
        return Config.houndClawQiCost;
    }

    @Override
    public int cooldownTicks() {
        return Math.min(Config.houndClawCooldownTicks, 13);
    }

    @Override
    public List<ItemStack> unlockCost() {
        return List.of(SpellScrollRecipes.get(id()).output().copy());
    }

    @Override
    public boolean cast(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        double range = Config.houndClawRange;
        double minDot = Math.cos(Math.toRadians(Config.houndClawAngle / 2.0D));
        AABB box = player.getBoundingBox().inflate(range, 1.5D, range);
        LivingEntity target = player.level().getEntitiesOfClass(LivingEntity.class, box,
                        entity -> entity != player && entity.isAlive() && player.distanceTo(entity) <= range)
                .stream()
                .filter(entity -> look.dot(entity.position().subtract(player.position()).normalize()) >= minDot)
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
        if (target == null) {
            return true;
        }

        float damage = (float) Config.houndClawDamage;
        int drain = Config.houndClawDrainQi;
        int maxQi = Math.max(1, QiManager.getMaxQi(target));
        if (QiManager.getQi(target) / (double) maxQi < Config.houndLowQiThreshold) {
            damage += (float) Config.houndLowQiBonusDamage;
            drain += Config.houndLowQiBonusDrain;
        }

        SkillManager.applySkillDamage(player, target, damage);
        SkillManager.applySkillQiDrain(player, target, drain);
        AdvancedSkillStateManager.markHoundTarget(player, target, Config.houndMarkTicks);
        return true;
    }
}
