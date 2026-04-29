package com.example.examplemod.skill.impl;

import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SkillRank;
import com.example.examplemod.skill.SkillRegistry;
import com.example.examplemod.skill.SkillSeries;
import com.example.examplemod.skill.SpellScrollRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class UnimplementedSkill implements Skill {
    private final String id;
    private final SkillSeries series;
    private final SkillRank rank;

    public UnimplementedSkill(String id, SkillSeries series, SkillRank rank) {
        this.id = id;
        this.series = series;
        this.rank = rank;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars." + id);
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars." + id + ".desc");
    }

    @Override
    public ItemStack icon() {
        SpellScrollRecipes.Recipe recipe = SpellScrollRecipes.get(id);
        if (recipe != null) {
            return recipe.output().copy();
        }
        String baseSkill = switch (series) {
            case PEGASUS -> SkillRegistry.PEGASUS_STEP;
            case ICE_DIPPER -> SkillRegistry.ICE_DIPPER_SHOT;
            case HOUND_CLAW -> SkillRegistry.HOUND_CLAW;
            default -> id;
        };
        recipe = SpellScrollRecipes.get(baseSkill);
        return recipe == null ? new ItemStack(Items.PAPER) : recipe.output().copy();
    }

    @Override
    public int tier() {
        return 1;
    }

    @Override
    public SkillSeries series() {
        return series;
    }

    @Override
    public SkillRank rank() {
        return rank;
    }

    @Override
    public int qiCost() {
        return 0;
    }

    @Override
    public int cooldownTicks() {
        return 0;
    }

    @Override
    public List<ItemStack> unlockCost() {
        String baseSkill = switch (series) {
            case PEGASUS -> SkillRegistry.PEGASUS_STEP;
            case ICE_DIPPER -> SkillRegistry.ICE_DIPPER_SHOT;
            case HOUND_CLAW -> SkillRegistry.HOUND_CLAW;
            default -> id;
        };
        SpellScrollRecipes.Recipe recipe = SpellScrollRecipes.get(baseSkill);
        return recipe == null ? List.of() : List.of(recipe.output().copy());
    }

    @Override
    public boolean cast(ServerPlayer player) {
        player.displayClientMessage(Component.translatable("message.sevenstars.skill_not_implemented"), true);
        return false;
    }
}
