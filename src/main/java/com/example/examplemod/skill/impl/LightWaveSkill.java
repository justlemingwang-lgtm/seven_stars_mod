package com.example.examplemod.skill.impl;

import com.example.examplemod.Config;
import com.example.examplemod.entity.LightWaveProjectileEntity;
import com.example.examplemod.registry.ModEntities;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SpellScrollRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class LightWaveSkill implements Skill {
    @Override
    public String id() {
        return "light_wave";
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars.light_wave");
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars.light_wave.desc");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModItems.LIGHT_WAVE_SCROLL.get());
    }

    @Override
    public int qiCost() {
        return Config.lightWaveQiCost;
    }

    @Override
    public int cooldownTicks() {
        return Config.lightWaveCooldownTicks;
    }

    @Override
    public List<ItemStack> unlockCost() {
        return List.of(SpellScrollRecipes.get(id()).output().copy());
    }

    @Override
    public boolean cast(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        LightWaveProjectileEntity projectile = new LightWaveProjectileEntity(ModEntities.LIGHT_WAVE_PROJECTILE.get(), level);
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.15D, player.getZ());
        projectile.setSkillValues((float) Config.lightWaveDamage, Config.lightWaveRange, 0);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.8F, 0.4F);
        level.addFreshEntity(projectile);
        return true;
    }
}
