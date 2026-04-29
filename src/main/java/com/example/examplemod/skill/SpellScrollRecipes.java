package com.example.examplemod.skill;

import com.example.examplemod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpellScrollRecipes {
    private static final Map<String, Recipe> RECIPES = new LinkedHashMap<>();

    static {
        register(SkillRegistry.LIGHT_WAVE, new ItemStack(ModItems.LIGHT_WAVE_SCROLL.get()),
                List.of(new ItemStack(ModItems.SPELL_FRAGMENT.get(), 1), new ItemStack(Items.PAPER, 2), new ItemStack(Items.GLOWSTONE_DUST, 8)));
        register(SkillRegistry.SEVEN_STAR, new ItemStack(ModItems.SEVEN_STAR_SKILL_SCROLL.get()),
                List.of(new ItemStack(ModItems.SPELL_FRAGMENT.get(), 2), new ItemStack(Items.PAPER, 2), new ItemStack(Items.LAPIS_LAZULI, 12), new ItemStack(Items.GOLD_INGOT, 2)));
        register(SkillRegistry.SIX_MERIDIAN_SWORD, new ItemStack(ModItems.SIX_MERIDIAN_SWORD_SCROLL.get()),
                List.of(new ItemStack(ModItems.SPELL_FRAGMENT.get(), 3), new ItemStack(Items.PAPER, 2), new ItemStack(Items.IRON_INGOT, 8), new ItemStack(Items.AMETHYST_SHARD, 4)));
        register(SkillRegistry.GOLDEN_FINGER, new ItemStack(ModItems.GOLDEN_FINGER_SCROLL.get()),
                List.of(new ItemStack(ModItems.SPELL_FRAGMENT.get(), 4), new ItemStack(Items.PAPER, 2), new ItemStack(Items.GOLD_INGOT, 8), new ItemStack(Items.AMETHYST_SHARD, 8)));
        register(SkillRegistry.PEGASUS_STEP, new ItemStack(ModItems.PEGASUS_STEP_SCROLL.get()), 1, SkillSeries.PEGASUS, SkillRank.NORMAL,
                List.of(new ItemStack(ModItems.SPELL_FRAGMENT.get(), 2), new ItemStack(Items.PAPER, 3), new ItemStack(ModItems.QI_CRYSTAL_SHARD.get(), 6), new ItemStack(ModItems.PEGASUS_MANE.get(), 2), new ItemStack(ModItems.STAR_HOOF_FRAGMENT.get(), 1)));
        register(SkillRegistry.ICE_DIPPER_SHOT, new ItemStack(ModItems.ICE_DIPPER_SHOT_SCROLL.get()), 1, SkillSeries.ICE_DIPPER, SkillRank.NORMAL,
                List.of(new ItemStack(ModItems.SPELL_FRAGMENT.get(), 2), new ItemStack(Items.PAPER, 3), new ItemStack(ModItems.QI_CRYSTAL_SHARD.get(), 4), new ItemStack(ModItems.FROST_SHELL_FRAGMENT.get(), 2), new ItemStack(ModItems.FROST_POWDER.get(), 2)));
        register(SkillRegistry.HOUND_CLAW, new ItemStack(ModItems.HOUND_CLAW_SCROLL.get()), 1, SkillSeries.HOUND_CLAW, SkillRank.NORMAL,
                List.of(new ItemStack(ModItems.SPELL_FRAGMENT.get(), 2), new ItemStack(Items.PAPER, 3), new ItemStack(ModItems.QI_CRYSTAL_SHARD.get(), 5), new ItemStack(ModItems.BLACK_MANE.get(), 2), new ItemStack(ModItems.CRACKED_CLAW_BONE.get(), 1)));
        register(SkillRegistry.GOAT_HORN, new ItemStack(ModItems.GOAT_HORN_SCROLL.get()), 2, SkillSeries.BASIC, SkillRank.NORMAL,
                List.of(new ItemStack(ModItems.SPELL_FRAGMENT.get(), 2), new ItemStack(Items.PAPER, 3), new ItemStack(ModItems.GOAT_HORN_CORE.get(), 1), new ItemStack(ModItems.COMPLETE_GOAT_HORN.get(), 1), new ItemStack(ModItems.STAR_RUNE_SHARD.get(), 2)));
        register(SkillRegistry.CHOP, new ItemStack(ModItems.CHOP_SCROLL.get()), 2, SkillSeries.BASIC, SkillRank.NORMAL,
                List.of(new ItemStack(ModItems.SPELL_FRAGMENT.get(), 1), new ItemStack(Items.PAPER, 2), new ItemStack(ModItems.DULL_BLADE_FRAGMENT.get(), 2), new ItemStack(ModItems.SHIELDBREAKER_BONE.get(), 1)));
        register(SkillRegistry.SEVEN_SCATTERED_STRIKES, new ItemStack(ModItems.TRIANGLE_SCROLL.get()), 2, SkillSeries.BASIC, SkillRank.NORMAL,
                List.of(new ItemStack(ModItems.SPELL_FRAGMENT.get(), 2), new ItemStack(Items.PAPER, 3), new ItemStack(ModItems.TRIANGLE_CORE.get(), 1), new ItemStack(ModItems.TRIANGLE_FRAGMENT.get(), 3)));
    }

    private static void register(String skillId, ItemStack output, List<ItemStack> cost) {
        RECIPES.put(skillId, new Recipe(skillId, output, 0, SkillSeries.BASIC, SkillRank.NORMAL, cost, 0,
                Component.empty()));
    }

    private static void register(String skillId, ItemStack output, int tier, SkillSeries series, SkillRank rank, List<ItemStack> cost) {
        RECIPES.put(skillId, new Recipe(skillId, output, tier, series, rank, cost, tier,
                Component.translatable("message.sevenstars.need_unlock_all_basic")));
    }

    public static Recipe get(String skillId) {
        return RECIPES.get(skillId);
    }

    public static Collection<Recipe> all() {
        return RECIPES.values();
    }

    public record Recipe(String skillId, ItemStack output, int tier, SkillSeries series, SkillRank rank,
                         List<ItemStack> cost, int requiredTier, Component unlockRequirementText) {
        public boolean unlocksSeries() {
            return tier > 0 && series != SkillSeries.BASIC;
        }

        public Component displayName() {
            return unlocksSeries() ? SkillEntryHelper.seriesDisplayName(series) : output.getHoverName();
        }
    }
}
