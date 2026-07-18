package com.example.examplemod.skill.impl;

import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SpellScrollRecipes;
import com.example.examplemod.stage2.GoatHornSpikeCaster;
import com.example.examplemod.stage2.SkillDisableManager;
import com.example.examplemod.stage2.SkillIds;
import com.example.examplemod.stage2.Stage2Constants;
import com.example.examplemod.stage3.ArmorDisableManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GoatHornSkill implements Skill {
    @Override
    public String id() {
        return SkillIds.GOAT_HORN;
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars.goat_horn");
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars.goat_horn.desc");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModItems.GOAT_HORN_SCROLL.get());
    }

    @Override
    public int tier() {
        return 2;
    }

    @Override
    public int qiCost() {
        return Stage2Constants.GOAT_HORN_SPIKES_QI_COST;
    }

    @Override
    public int cooldownTicks() {
        return Stage2Constants.GOAT_HORN_SPIKES_COOLDOWN_TICKS;
    }

    @Override
    public List<ItemStack> unlockCost() {
        return List.of(SpellScrollRecipes.get(id()).output().copy());
    }

    @Override
    public boolean canCast(ServerPlayer player) {
        if (ArmorDisableManager.isArmorDisabled(player)) {
            player.displayClientMessage(Component.translatable("message.sevenstars.armor_disabled"), true);
            return false;
        }
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GOAT_HORN_ARMOR.get())) {
            player.displayClientMessage(Component.translatable("message.sevenstars.goat_horn_spikes_need_armor"), true);
            return false;
        }
        if (SkillDisableManager.isSkillDisabled(player, SkillIds.GOAT_HORN)) {
            player.displayClientMessage(Component.translatable("message.sevenstars.goat_horn_disabled"), true);
            return false;
        }
        return true;
    }

    @Override
    public boolean cast(ServerPlayer player) {
        return GoatHornSpikeCaster.cast(player);
    }
}
