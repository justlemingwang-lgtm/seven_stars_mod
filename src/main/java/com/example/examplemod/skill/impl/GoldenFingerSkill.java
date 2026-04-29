package com.example.examplemod.skill.impl;

import com.example.examplemod.Config;
import com.example.examplemod.entity.GoldenFingerProjectileEntity;
import com.example.examplemod.registry.ModEntities;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SpellScrollRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GoldenFingerSkill implements Skill {
    @Override
    public String id() {
        return "golden_finger";
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars.golden_finger");
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars.golden_finger.desc");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModItems.GOLDEN_FINGER_SCROLL.get());
    }

    @Override
    public int qiCost() {
        return Config.goldenFingerQiCost;
    }

    @Override
    public int cooldownTicks() {
        return Config.goldenFingerCooldownTicks;
    }

    @Override
    public List<ItemStack> unlockCost() {
        return List.of(SpellScrollRecipes.get(id()).output().copy());
    }

    @Override
    public boolean cast(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        GoldenFingerProjectileEntity projectile = new GoldenFingerProjectileEntity(ModEntities.GOLDEN_FINGER_PROJECTILE.get(), level);
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.15D, player.getZ());
        projectile.setSkillValues((float) Config.goldenFingerDamage, Config.goldenFingerRange, Config.goldenFingerDrainQi);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.35F, 0.35F);
        level.addFreshEntity(projectile);
        return true;
    }
}
