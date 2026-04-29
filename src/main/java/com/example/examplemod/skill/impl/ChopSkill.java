package com.example.examplemod.skill.impl;

import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SpellScrollRecipes;
import com.example.examplemod.stage2.ChopSkillLogic;
import com.example.examplemod.stage2.SkillIds;
import com.example.examplemod.stage2.Stage2Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ChopSkill implements Skill {
    @Override
    public String id() {
        return SkillIds.CHOP;
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars.chop");
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars.chop.desc");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModItems.CHOP_SCROLL.get());
    }

    @Override
    public int tier() {
        return 2;
    }

    @Override
    public int qiCost() {
        return Stage2Constants.CHOP_QI_COST;
    }

    @Override
    public int cooldownTicks() {
        return Stage2Constants.CHOP_COOLDOWN_TICKS;
    }

    @Override
    public List<ItemStack> unlockCost() {
        return List.of(SpellScrollRecipes.get(id()).output().copy());
    }

    @Override
    public boolean cast(ServerPlayer player) {
        int hits = ChopSkillLogic.performChop(player, Stage2Constants.CHOP_DAMAGE);
        if (hits == 0) {
            player.displayClientMessage(Component.translatable("message.sevenstars.chop_missed"), true);
        }
        return true;
    }
}
