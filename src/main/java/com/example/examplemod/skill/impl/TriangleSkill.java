package com.example.examplemod.skill.impl;

import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SpellScrollRecipes;
import com.example.examplemod.stage2.SkillIds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TriangleSkill implements Skill {
    private static final int QI_COST = 20;
    private static final int COOLDOWN_TICKS = 200;

    @Override
    public String id() {
        return SkillIds.TRIANGLE;
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars.triangle");
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars.triangle.desc");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModItems.TRIANGLE_SCROLL.get());
    }

    @Override
    public int tier() {
        return 2;
    }

    @Override
    public int qiCost() {
        return QI_COST;
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public List<ItemStack> unlockCost() {
        return List.of(SpellScrollRecipes.get(id()).output().copy());
    }

    @Override
    public boolean cast(ServerPlayer player) {
        player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.45D, 0.35D, 0.45D, 0.02D);
        player.displayClientMessage(Component.translatable("message.sevenstars.triangle_placeholder"), true);
        return true;
    }
}
