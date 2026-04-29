package com.example.examplemod.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.List;

public interface Skill {
    String id();

    Component displayName();

    Component description();

    ItemStack icon();

    default int tier() {
        return 0;
    }

    default SkillSeries series() {
        return SkillSeries.BASIC;
    }

    default SkillRank rank() {
        return SkillRank.NORMAL;
    }

    default boolean unlocksSeries() {
        return tier() > 0 && series() != SkillSeries.BASIC;
    }

    default boolean isUltimate() {
        return rank() == SkillRank.ULTIMATE;
    }

    default Item requiredScroll() {
        return unlockCost().isEmpty() ? null : unlockCost().get(0).getItem();
    }

    default List<String> prerequisiteSkills() {
        return Collections.emptyList();
    }

    int qiCost();

    int cooldownTicks();

    List<ItemStack> unlockCost();

    default boolean canCast(ServerPlayer player) {
        return true;
    }

    boolean cast(ServerPlayer player);
}
