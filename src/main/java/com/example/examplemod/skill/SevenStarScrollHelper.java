package com.example.examplemod.skill;

import com.example.examplemod.Config;
import com.example.examplemod.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SevenStarScrollHelper {
    private static final String SKILLS_TAG = "SevenStarSkills";

    public static int getSlotCount(ItemStack stack) {
        return stack.is(ModItems.SEVEN_STAR_SCROLL.get()) ? Config.sevenStarScrollBaseSlots : 0;
    }

    public static List<String> getSkills(ItemStack stack) {
        int slots = getSlotCount(stack);
        List<String> skills = new ArrayList<>();
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList(SKILLS_TAG, Tag.TAG_STRING);
        for (int i = 0; i < slots; i++) {
            skills.add(i < list.size() ? list.getString(i) : "");
        }
        return skills;
    }

    public static String getSkill(ItemStack stack, int slot) {
        List<String> skills = getSkills(stack);
        return slot >= 0 && slot < skills.size() ? skills.get(slot) : "";
    }

    public static void setSkill(ItemStack stack, int slot, String skillId) {
        List<String> skills = getSkills(stack);
        if (slot < 0 || slot >= skills.size()) {
            return;
        }
        skills.set(slot, skillId == null ? "" : skillId);
        saveSkills(stack, skills);
    }

    public static boolean containsSkill(ItemStack stack, String skillId) {
        return getSkills(stack).contains(skillId);
    }

    private static void saveSkills(ItemStack stack, List<String> skills) {
        ListTag list = new ListTag();
        for (String skillId : skills) {
            list.add(StringTag.valueOf(skillId));
        }
        stack.getOrCreateTag().put(SKILLS_TAG, list);
    }
}
