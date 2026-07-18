package com.example.examplemod.skill.impl;

import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SpellScrollRecipes;
import com.example.examplemod.stage2.SevenScatteredStrikesCaster;
import com.example.examplemod.stage2.SkillDisableManager;
import com.example.examplemod.stage2.SkillIds;
import com.example.examplemod.stage3.ArmorDisableManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TriangleSkill implements Skill {
    @Override
    public String id() {
        return SkillIds.SEVEN_SCATTERED_STRIKES;
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars.seven_scattered_strikes");
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars.seven_scattered_strikes.desc");
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
        return com.example.examplemod.stage2.Stage2Constants.SEVEN_SCATTERED_STRIKES_QI_COST;
    }

    @Override
    public int cooldownTicks() {
        return com.example.examplemod.stage2.Stage2Constants.SEVEN_SCATTERED_STRIKES_COOLDOWN_TICKS;
    }

    @Override
    public List<ItemStack> unlockCost() {
        return List.of(SpellScrollRecipes.get(id()).output().copy());
    }

    @Override
    public boolean cast(ServerPlayer player) {
        SevenScatteredStrikesCaster.cast(player);
        return true;
    }

    @Override
    public boolean canCast(ServerPlayer player) {
        if (ArmorDisableManager.isArmorDisabled(player)) {
            player.displayClientMessage(Component.translatable("message.sevenstars.armor_disabled"), true);
            return false;
        }
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.TRIANGLE_ARMOR.get())) {
            player.displayClientMessage(Component.translatable("message.sevenstars.triangle_need_armor"), true);
            return false;
        }
        if (SkillDisableManager.isSkillDisabled(player, SkillIds.TRIANGLE)) {
            player.displayClientMessage(Component.translatable("message.sevenstars.triangle_disabled"), true);
            return false;
        }
        return true;
    }
}
