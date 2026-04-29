package com.example.examplemod.skill;

import com.example.examplemod.Config;
import com.example.examplemod.network.ModNetwork;
import com.example.examplemod.network.SyncPlayerSkillDataPacket;
import com.example.examplemod.qi.QiManager;
import com.example.examplemod.registry.ModBlocks;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.stage2.SkillDisableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;
import java.util.Set;

public class SkillManager {
    private static boolean applyingQiCollapseDamage;

    public static Optional<PlayerSkillData> getData(ServerPlayer player) {
        return player.getCapability(PlayerSkillProvider.SKILL_CAPABILITY).resolve();
    }

    public static void sync(ServerPlayer player) {
        getData(player).ifPresent(data -> ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncPlayerSkillDataPacket(data.getUnlockedSkills(), data.getUnlockedSeries())));
    }

    public static void unlock(ServerPlayer player, String skillId) {
        Optional<Skill> skillOptional = SkillRegistry.get(skillId);
        if (skillOptional.isEmpty()) {
            return;
        }
        Skill skill = skillOptional.get();
        getData(player).ifPresent(data -> {
            if (data.isUnlocked(skillId)) {
                player.displayClientMessage(Component.translatable("message.sevenstars.skill_already_unlocked"), true);
                return;
            }
            if (skill.unlocksSeries() && data.isSeriesUnlocked(skill.series())) {
                player.displayClientMessage(Component.translatable("message.sevenstars.skill_already_unlocked"), true);
                return;
            }
            if (!SkillTierManager.canAccessTier(data, skill.tier())) {
                player.displayClientMessage(Component.translatable("message.sevenstars.need_unlock_all_basic"), true);
                return;
            }
            if (!hasMaterials(player, skill)) {
                player.displayClientMessage(Component.translatable("message.sevenstars.missing_spell_scroll"), true);
                return;
            }
            consumeMaterials(player, skill);
            unlockResolved(data, skill);
            sync(player);
            player.displayClientMessage(unlockSuccessMessage(skill), true);
        });
    }

    public static void unlockWithScroll(ServerPlayer player, String skillId, ItemStack scrollStack) {
        SkillRegistry.get(skillId).ifPresent(skill -> getData(player).ifPresent(data -> {
            if (data.isUnlocked(skillId) || (skill.unlocksSeries() && data.isSeriesUnlocked(skill.series()))) {
                player.displayClientMessage(Component.translatable("message.sevenstars.skill_already_unlocked"), true);
                return;
            }
            if (!SkillTierManager.canAccessTier(data, skill.tier())) {
                player.displayClientMessage(Component.translatable("message.sevenstars.need_unlock_all_basic"), true);
                return;
            }
            unlockResolved(data, skill);
            if (!player.isCreative()) {
                scrollStack.shrink(1);
            }
            sync(player);
            player.displayClientMessage(unlockSuccessMessage(skill), true);
        }));
    }

    public static void writeSkill(ServerPlayer player, InteractionHand hand, int slot, String skillId) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.SEVEN_STAR_SCROLL.get())) {
            return;
        }
        getData(player).ifPresent(data -> {
            Optional<Skill> skill = SkillRegistry.get(skillId);
            if (skill.isEmpty()) {
                return;
            }
            String entry = skill.get().unlocksSeries() ? SkillEntryHelper.seriesEntry(skill.get().series()) : skillId;
            if (skill.get().unlocksSeries() ? !data.isSeriesUnlocked(skill.get().series()) : !data.isUnlocked(skillId)) {
                player.displayClientMessage(Component.translatable("message.sevenstars.write_locked"), true);
                return;
            }
            SevenStarScrollHelper.setSkill(stack, slot, entry);
            player.displayClientMessage(Component.translatable("message.sevenstars.skill_written"), true);
        });
    }

    public static void removeSkill(ServerPlayer player, InteractionHand hand, int slot) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.SEVEN_STAR_SCROLL.get())) {
            return;
        }
        SevenStarScrollHelper.setSkill(stack, slot, "");
        player.displayClientMessage(Component.translatable("message.sevenstars.skill_slot_cleared"), true);
    }

    public static void castSlot(ServerPlayer player, int slot, SkillRank rank) {
        ItemStack scroll = findUsableScroll(player);
        if (scroll.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.sevenstars.no_scroll"), true);
            return;
        }
        String entry = SevenStarScrollHelper.getSkill(scroll, slot);
        if (entry.isBlank()) {
            player.displayClientMessage(Component.translatable("message.sevenstars.skill_slot_empty"), true);
            return;
        }
        Optional<SkillSeries> series = SkillEntryHelper.parseSeriesEntry(entry);
        if (series.isPresent()) {
            castSeries(player, scroll, entry, series.get(), rank);
        } else {
            castSkill(player, scroll, entry);
        }
    }

    public static void castSlot(ServerPlayer player, int slot) {
        castSlot(player, slot, SkillRank.NORMAL);
    }

    public static void castSkill(ServerPlayer player, ItemStack scroll, String skillId) {
        Optional<Skill> skillOptional = SkillRegistry.get(skillId);
        if (skillOptional.isEmpty()) {
            return;
        }
        Skill skill = skillOptional.get();
        getData(player).ifPresent(data -> {
            long gameTime = player.serverLevel().getGameTime();
            if (!data.isUnlocked(skillId)) {
                player.displayClientMessage(Component.translatable("message.sevenstars.skill_locked"), true);
                return;
            }
            if (!SevenStarScrollHelper.containsSkill(scroll, skillId)) {
                player.displayClientMessage(Component.translatable("message.sevenstars.skill_not_written"), true);
                return;
            }
            if (QiManager.getQi(player) < skill.qiCost()) {
                player.displayClientMessage(Component.translatable("message.sevenstars.not_enough_qi"), true);
                return;
            }
            if (!hasCooldownOverride(player) && data.isOnCooldown(skillId, gameTime)) {
                player.displayClientMessage(Component.translatable("message.sevenstars.skill_cooldown"), true);
                return;
            }
            if (CastStateManager.isCasting(player)) {
                player.displayClientMessage(Component.translatable("message.sevenstars.cannot_cast_now"), true);
                return;
            }
            if (!skill.canCast(player)) {
                return;
            }
            if (!QiManager.consumeQi(player, skill.qiCost())) {
                player.displayClientMessage(Component.translatable("message.sevenstars.not_enough_qi"), true);
                return;
            }
            if (skill.cast(player)) {
                int cooldown = effectiveCooldown(player, skill);
                if (cooldown > 0) {
                    data.setCooldown(skillId, gameTime + cooldown);
                    SkillDisableManager.setCooldown(player, skillId, cooldown);
                }
            }
        });
    }

    private static void castSeries(ServerPlayer player, ItemStack scroll, String entry, SkillSeries series, SkillRank rank) {
        getData(player).ifPresent(data -> {
            if (!SevenStarScrollHelper.containsSkill(scroll, entry)) {
                player.displayClientMessage(Component.translatable("message.sevenstars.skill_not_written"), true);
                return;
            }
            if (!data.isSeriesUnlocked(series)) {
                player.displayClientMessage(Component.translatable("message.sevenstars.skill_locked"), true);
                return;
            }
            Optional<Skill> skillOptional = SkillRegistry.getBySeriesRank(series, rank);
            if (skillOptional.isEmpty() || rank != SkillRank.NORMAL) {
                player.displayClientMessage(Component.translatable("message.sevenstars.rank_not_implemented"), true);
                return;
            }
            Skill skill = skillOptional.get();
            long gameTime = player.serverLevel().getGameTime();
            if (QiManager.getQi(player) < skill.qiCost()) {
                player.displayClientMessage(Component.translatable("message.sevenstars.not_enough_qi"), true);
                return;
            }
            if (!hasCooldownOverride(player) && data.isOnCooldown(skill.id(), gameTime)) {
                player.displayClientMessage(Component.translatable("message.sevenstars.skill_cooldown"), true);
                return;
            }
            if (CastStateManager.isCasting(player)) {
                player.displayClientMessage(Component.translatable("message.sevenstars.cannot_cast_now"), true);
                return;
            }
            if (!skill.canCast(player)) {
                return;
            }
            if (!QiManager.consumeQi(player, skill.qiCost())) {
                player.displayClientMessage(Component.translatable("message.sevenstars.not_enough_qi"), true);
                return;
            }
            if (skill.cast(player)) {
                int cooldown = effectiveCooldown(player, skill);
                if (cooldown > 0) {
                    data.setCooldown(skill.id(), gameTime + cooldown);
                    SkillDisableManager.setCooldown(player, skill.id(), cooldown);
                }
            }
        });
    }

    public static void applySkillQiDrain(ServerPlayer caster, LivingEntity target, int amount) {
        applyFormalQiDrain(caster, target, amount);
    }

    public static void applyCombatQiDrain(LivingEntity target, int amount) {
        applyFormalQiDrain(null, target, amount);
    }

    public static boolean isApplyingQiCollapseDamage() {
        return applyingQiCollapseDamage;
    }

    private static void applyFormalQiDrain(ServerPlayer notifier, LivingEntity target, int amount) {
        int before = QiManager.getQi(target);
        int after = before - amount;
        if (Config.enableNegativeQiPenalty && after < 0) {
            float damage = (float) (target.getMaxHealth() * Config.negativeQiHealthPercentDamage);
            // TODO: 后续加入玩家气崩溃保护机制，例如崩溃冷却、短暂无敌、短时间内不重复触发等。
            applyingQiCollapseDamage = true;
            try {
                target.hurt(target.damageSources().magic(), damage);
            } finally {
                applyingQiCollapseDamage = false;
            }
            int protectedQi = Math.max(1, (int) Math.floor(QiManager.getMaxQi(target) * Config.negativeQiProtectionRecoverPercent));
            QiManager.setQi(target, protectedQi);
            if (notifier != null) {
                notifier.displayClientMessage(Component.translatable("message.sevenstars.qi_break_protected"), true);
            }
        } else {
            QiManager.setQi(target, Math.max(0, after));
        }
    }

    public static void applySkillDamage(ServerPlayer caster, LivingEntity target, float damage) {
        target.hurt(caster.damageSources().magic(), AdvancedSkillStateManager.applyHoundMarkBonus(caster, target, damage));
    }

    public static ItemStack findUsableScroll(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.is(ModItems.SEVEN_STAR_SCROLL.get())) {
            return main;
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.SEVEN_STAR_SCROLL.get())) {
            return offhand;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.SEVEN_STAR_SCROLL.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void writeSpellScroll(ServerPlayer player, String skillId, BlockPos tablePos) {
        if (!player.level().getBlockState(tablePos).is(ModBlocks.WRITING_TABLE.get()) || player.distanceToSqr(tablePos.getCenter()) > 64.0D) {
            return;
        }
        SpellScrollRecipes.Recipe recipe = SpellScrollRecipes.get(skillId);
        if (recipe == null) {
            return;
        }
        Optional<Skill> skill = SkillRegistry.get(skillId);
        if (skill.isEmpty()) {
            return;
        }
        Optional<PlayerSkillData> skillData = getData(player);
        if (skillData.isEmpty()) {
            return;
        }
        if (!SkillTierManager.canAccessTier(skillData.get(), recipe.requiredTier())) {
            player.displayClientMessage(Component.translatable("message.sevenstars.need_unlock_all_basic"), true);
            return;
        }
        if (skillData.get().isUnlocked(skillId) || (skill.get().unlocksSeries() && skillData.get().isSeriesUnlocked(skill.get().series()))) {
            player.displayClientMessage(Component.translatable("message.sevenstars.skill_already_unlocked"), true);
            return;
        }
        if (!hasMaterials(player, recipe.cost())) {
            player.displayClientMessage(Component.translatable("message.sevenstars.write_scroll_not_enough"), true);
            return;
        }
        consumeMaterials(player, recipe.cost());
        if (!player.getInventory().add(recipe.output().copy())) {
            player.drop(recipe.output().copy(), false);
        }
        player.displayClientMessage(Component.translatable("message.sevenstars.write_scroll_success", recipe.displayName()), true);
    }

    private static boolean hasMaterials(ServerPlayer player, Skill skill) {
        return hasMaterials(player, skill.unlockCost());
    }

    private static boolean hasMaterials(ServerPlayer player, Iterable<ItemStack> costs) {
        for (ItemStack cost : costs) {
            if (player.getInventory().countItem(cost.getItem()) < cost.getCount()) {
                return false;
            }
        }
        return true;
    }

    private static void consumeMaterials(ServerPlayer player, Skill skill) {
        consumeMaterials(player, skill.unlockCost());
    }

    private static void consumeMaterials(ServerPlayer player, Iterable<ItemStack> costs) {
        if (player.isCreative()) {
            return;
        }
        for (ItemStack cost : costs) {
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(cost.getItem()), cost.getCount(), player.inventoryMenu.getCraftSlots());
        }
    }

    public static void copyUnlocked(PlayerSkillData target, Set<String> skills) {
        skills.forEach(target::unlock);
    }

    private static void unlockResolved(PlayerSkillData data, Skill skill) {
        data.unlock(skill.id());
        if (skill.unlocksSeries()) {
            data.unlockSeries(skill.series());
            SkillRegistry.skillsForSeries(skill.series()).forEach(seriesSkill -> data.unlock(seriesSkill.id()));
        }
    }

    private static Component unlockSuccessMessage(Skill skill) {
        if (skill.unlocksSeries()) {
            return Component.translatable("message.sevenstars.unlock_series_success", SkillEntryHelper.seriesDisplayName(skill.series()));
        }
        return Component.translatable("message.sevenstars.unlock_success", skill.displayName());
    }

    private static int effectiveCooldown(ServerPlayer player, Skill skill) {
        if (hasCooldownOverride(player)) {
            return 0;
        }
        return skill.cooldownTicks();
    }

    private static boolean hasCooldownOverride(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.COOLDOWN_CHESTPLATE.get());
    }
}
