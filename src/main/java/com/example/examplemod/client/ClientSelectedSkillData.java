package com.example.examplemod.client;

import com.example.examplemod.skill.SevenStarScrollHelper;
import com.example.examplemod.skill.SkillEntryHelper;
import com.example.examplemod.skill.SkillRank;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ClientSelectedSkillData {
    private static int selectedSlot = -1;
    private static SkillRank selectedRank = SkillRank.NORMAL;

    public static int getSelectedSlot() {
        return selectedSlot;
    }

    public static void setSelectedSlot(int slot) {
        selectedSlot = slot;
    }

    public static SkillRank getSelectedRank() {
        return selectedRank;
    }

    public static SkillRank cycleRank() {
        selectedRank = switch (selectedRank) {
            case NORMAL -> SkillRank.MEDIUM;
            case MEDIUM -> SkillRank.ULTIMATE;
            case ULTIMATE -> SkillRank.NORMAL;
        };
        return selectedRank;
    }

    public static Component getRankDisplayName() {
        return switch (selectedRank) {
            case NORMAL -> Component.translatable("rank.sevenstars.normal");
            case MEDIUM -> Component.translatable("rank.sevenstars.medium");
            case ULTIMATE -> Component.translatable("rank.sevenstars.ultimate");
        };
    }

    public static Component getDisplayName() {
        if (selectedSlot < 0) {
            return Component.translatable("hud.sevenstars.current_spell_none");
        }
        ItemStack scroll = findClientScroll();
        if (scroll.isEmpty()) {
            return Component.translatable("hud.sevenstars.current_spell_none");
        }
        String skillId = SevenStarScrollHelper.getSkill(scroll, selectedSlot);
        if (skillId.isBlank()) {
            return Component.translatable("hud.sevenstars.current_spell_empty");
        }
        return SkillEntryHelper.displayName(skillId);
    }

    public static ItemStack findClientScroll() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack main = minecraft.player.getMainHandItem();
        if (SevenStarScrollHelper.getSlotCount(main) > 0) {
            return main;
        }
        ItemStack offhand = minecraft.player.getOffhandItem();
        if (SevenStarScrollHelper.getSlotCount(offhand) > 0) {
            return offhand;
        }
        for (ItemStack stack : minecraft.player.getInventory().items) {
            if (SevenStarScrollHelper.getSlotCount(stack) > 0) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
