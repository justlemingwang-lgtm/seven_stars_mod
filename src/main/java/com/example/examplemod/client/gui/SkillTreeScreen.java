package com.example.examplemod.client.gui;

import com.example.examplemod.client.ClientSkillData;
import com.example.examplemod.network.ModNetwork;
import com.example.examplemod.network.RemoveSkillFromScrollPacket;
import com.example.examplemod.network.UnlockSkillPacket;
import com.example.examplemod.network.WriteSkillToScrollPacket;
import com.example.examplemod.skill.SevenStarScrollHelper;
import com.example.examplemod.skill.SkillEntryHelper;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SkillRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SkillTreeScreen extends Screen {
    private final InteractionHand hand;
    private Skill selectedSkill;
    private int selectedSlot = -1;
    private int pageTier;

    public SkillTreeScreen(Component title, InteractionHand hand) {
        super(title);
        this.hand = hand;
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        int left = 24;
        int top = 36;
        int i = 0;
        for (Skill skill : pageSkills()) {
            if (skill.tier() > 0 && skill.rank() != com.example.examplemod.skill.SkillRank.NORMAL) {
                continue;
            }
            int y = top + i * 28;
            boolean unlocked = skill.unlocksSeries() ? ClientSkillData.isSeriesUnlocked(skill.series()) : ClientSkillData.isUnlocked(skill.id());
            Component label = (skill.unlocksSeries() ? SkillEntryHelper.seriesDisplayName(skill.series()) : skill.displayName()).copy()
                    .withStyle(unlocked ? ChatFormatting.GREEN : ChatFormatting.GRAY);
            addRenderableWidget(Button.builder(label, button -> {
                selectedSkill = skill;
                rebuild();
            }).bounds(left, y, 126, 20).build());
            i++;
        }

        addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            if (pageTier > 0) {
                pageTier--;
                selectedSkill = null;
                rebuild();
            }
        }).bounds(24, 8, 22, 20).build()).active = pageTier > 0;

        addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            if (pageTier < maxTier()) {
                pageTier++;
                selectedSkill = null;
                rebuild();
            }
        }).bounds(52, 8, 22, 20).build()).active = pageTier < maxTier();

        addRenderableWidget(Button.builder(Component.translatable("button.sevenstars.unlock"), button -> {
            if (selectedSkill != null) {
                ModNetwork.CHANNEL.sendToServer(new UnlockSkillPacket(selectedSkill.id()));
            }
        }).bounds(width - 150, height - 54, 56, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("button.sevenstars.write"), button -> {
            if (selectedSkill != null && selectedSlot >= 0) {
                ModNetwork.CHANNEL.sendToServer(new WriteSkillToScrollPacket(hand, selectedSlot, selectedSkill.id()));
            }
        }).bounds(width - 88, height - 54, 56, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("button.sevenstars.clear"), button -> {
            if (selectedSlot >= 0) {
                ModNetwork.CHANNEL.sendToServer(new RemoveSkillFromScrollPacket(hand, selectedSlot));
            }
        }).bounds(width - 88, height - 30, 56, 20).build());

        List<String> slots = currentSlots();
        for (int slot = 0; slot < slots.size(); slot++) {
            int x = 24 + slot * 72;
            int y = height - 84;
            int finalSlot = slot;
            String skillId = slots.get(slot);
            Component slotLabel = Component.literal((slot + 1) + ": " + skillName(skillId));
            addRenderableWidget(Button.builder(slotLabel, button -> {
                selectedSlot = finalSlot;
                rebuild();
            }).bounds(x, y, 66, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("screen.sevenstars.tier_page", pageTier), 80, 14, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.sevenstars.skill_tree"), 24, 24, 0xFFFFFF, false);
        graphics.drawString(font, Component.literal("Slots"), 24, height - 100, 0xFFFFFF, false);

        int detailX = width / 2;
        int detailY = 38;
        int i = 0;
        for (Skill skill : pageSkills()) {
            if (skill.tier() > 0 && skill.rank() != com.example.examplemod.skill.SkillRank.NORMAL) {
                continue;
            }
            graphics.renderItem(skill.icon(), 28, 38 + i * 28);
            i++;
        }
        if (selectedSkill != null) {
            graphics.renderItem(selectedSkill.icon(), detailX - 22, detailY - 4);
            graphics.drawString(font, selectedSkill.displayName(), detailX, detailY, 0xFFE680, false);
            graphics.drawString(font, selectedSkill.description(), detailX, detailY + 16, 0xCCCCCC, false);
            graphics.drawString(font, Component.translatable("screen.sevenstars.skill_qi_cost", selectedSkill.qiCost()), detailX, detailY + 34, 0x66DDFF, false);
            graphics.drawString(font, Component.translatable("screen.sevenstars.skill_cooldown", selectedSkill.cooldownTicks()), detailX, detailY + 48, 0xCCCCCC, false);
            graphics.drawString(font, Component.translatable("screen.sevenstars.unlock_cost"), detailX, detailY + 66, 0xFFFFFF, false);
            int y = detailY + 82;
            for (ItemStack cost : selectedSkill.unlockCost()) {
                graphics.renderItem(cost, detailX, y - 4);
                graphics.drawString(font, Component.literal(cost.getCount() + " x ").append(cost.getHoverName()), detailX + 20, y, 0xCCCCCC, false);
                y += 18;
            }
        }
        if (selectedSlot >= 0) {
            graphics.drawString(font, Component.translatable("screen.sevenstars.selected_slot", selectedSlot + 1), 24, height - 116, 0xFFE680, false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private List<String> currentSlots() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return List.of();
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        return new ArrayList<>(SevenStarScrollHelper.getSkills(stack));
    }

    private String skillName(String skillId) {
        if (skillId == null || skillId.isBlank()) {
            return "-";
        }
        return SkillEntryHelper.displayName(skillId).getString();
    }

    private List<Skill> pageSkills() {
        return SkillRegistry.all().stream()
                .filter(skill -> skill.tier() == pageTier)
                .toList();
    }

    private int maxTier() {
        return SkillRegistry.all().stream().mapToInt(Skill::tier).max().orElse(0);
    }
}
