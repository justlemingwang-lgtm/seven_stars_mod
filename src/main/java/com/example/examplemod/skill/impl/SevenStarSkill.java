package com.example.examplemod.skill.impl;

import com.example.examplemod.Config;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.CastStateManager;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SpellScrollRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SevenStarSkill implements Skill {
    @Override
    public String id() {
        return "seven_star";
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars.seven_star");
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars.seven_star.desc");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModItems.SEVEN_STAR_SKILL_SCROLL.get());
    }

    @Override
    public int qiCost() {
        return Config.sevenStarQiCost;
    }

    @Override
    public int cooldownTicks() {
        return Config.sevenStarCooldownTicks;
    }

    @Override
    public List<ItemStack> unlockCost() {
        return List.of(SpellScrollRecipes.get(id()).output().copy());
    }

    @Override
    public boolean cast(ServerPlayer player) {
        CastStateManager.startSevenStar(player);
        return true;
    }
}
