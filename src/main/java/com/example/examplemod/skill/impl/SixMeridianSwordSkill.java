package com.example.examplemod.skill.impl;

import com.example.examplemod.Config;
import com.example.examplemod.qi.QiManager;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SkillManager;
import com.example.examplemod.skill.SpellScrollRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class SixMeridianSwordSkill implements Skill {
    @Override
    public String id() {
        return "six_meridian_sword";
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars.six_meridian_sword");
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars.six_meridian_sword.desc");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModItems.SIX_MERIDIAN_SWORD_SCROLL.get());
    }

    @Override
    public int qiCost() {
        return Config.sixMeridianSwordQiCost;
    }

    @Override
    public int cooldownTicks() {
        return Math.min(Config.sixMeridianSwordCooldownTicks, 13);
    }

    @Override
    public List<ItemStack> unlockCost() {
        return List.of(SpellScrollRecipes.get(id()).output().copy());
    }

    @Override
    public boolean cast(ServerPlayer player) {
        double range = Config.sixMeridianSwordRange;
        Vec3 look = player.getLookAngle().normalize();
        AABB box = player.getBoundingBox().inflate(range, 1.5D, range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, box,
                        target -> target != player && target.isAlive() && player.distanceTo(target) <= range)
                .stream()
                .filter(target -> isInFrontCone(player, target, look))
                .sorted(Comparator.comparingDouble(player::distanceToSqr))
                .toList();
        if (targets.isEmpty()) {
            return true;
        }
        LivingEntity target = targets.get(0);
        SkillManager.applySkillDamage(player, target, (float) Config.sixMeridianSwordDamage);
        SkillManager.applySkillQiDrain(player, target, Config.sixMeridianSwordDrainQi);
        int recover = (int) Math.floor(Config.sixMeridianSwordDrainQi * Config.sixMeridianSwordSelfRecoverRatio);
        QiManager.addQi(player, recover);
        return true;
    }

    private boolean isInFrontCone(ServerPlayer player, LivingEntity target, Vec3 look) {
        Vec3 direction = target.position().subtract(player.position()).normalize();
        return look.dot(direction) >= 0.5D;
    }
}
