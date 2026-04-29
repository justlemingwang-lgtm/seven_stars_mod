package com.example.examplemod.skill.impl;

import com.example.examplemod.Config;
import com.example.examplemod.entity.IceDipperProjectileEntity;
import com.example.examplemod.registry.ModEntities;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.AdvancedSkillStateManager;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SkillRank;
import com.example.examplemod.skill.SkillRegistry;
import com.example.examplemod.skill.SkillSeries;
import com.example.examplemod.skill.SpellScrollRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class IceDipperShotSkill implements Skill {
    @Override
    public String id() {
        return SkillRegistry.ICE_DIPPER_SHOT;
    }

    @Override
    public Component displayName() {
        return Component.translatable("skill.sevenstars.ice_dipper_shot");
    }

    @Override
    public Component description() {
        return Component.translatable("skill.sevenstars.ice_dipper_shot.desc");
    }

    @Override
    public ItemStack icon() {
        return new ItemStack(ModItems.ICE_DIPPER_SHOT_SCROLL.get());
    }

    @Override
    public int tier() {
        return 1;
    }

    @Override
    public SkillSeries series() {
        return SkillSeries.ICE_DIPPER;
    }

    @Override
    public SkillRank rank() {
        return SkillRank.NORMAL;
    }

    @Override
    public int qiCost() {
        return Config.iceDipperQiCost;
    }

    @Override
    public int cooldownTicks() {
        return Config.iceDipperCooldownTicks;
    }

    @Override
    public List<ItemStack> unlockCost() {
        return List.of(SpellScrollRecipes.get(id()).output().copy());
    }

    @Override
    public boolean cast(ServerPlayer player) {
        double charge = AdvancedSkillStateManager.getIceChargeRatio(player);
        float damage = (float) (Config.iceDipperMinDamage + (Config.iceDipperMaxDamage - Config.iceDipperMinDamage) * charge);
        ServerLevel level = player.serverLevel();
        IceDipperProjectileEntity projectile = new IceDipperProjectileEntity(ModEntities.ICE_DIPPER_PROJECTILE.get(), level);
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.15D, player.getZ());
        projectile.setSkillValues(damage, Config.iceDipperRange, Config.iceDipperDrainQi, Config.iceDipperSlowTicks);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, (float) Config.iceDipperProjectileSpeed, 0.25F);
        level.addFreshEntity(projectile);
        AdvancedSkillStateManager.resetIceCharge(player);
        return true;
    }
}
