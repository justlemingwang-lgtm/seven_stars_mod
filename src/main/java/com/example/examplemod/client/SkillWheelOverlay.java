package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.skill.SevenStarScrollHelper;
import com.example.examplemod.skill.Skill;
import com.example.examplemod.skill.SkillEntryHelper;
import com.example.examplemod.skill.SkillRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SkillWheelOverlay {
    private static final IGuiOverlay OVERLAY = SkillWheelOverlay::render;
    private static boolean active;
    private static double anchorMouseX;
    private static double anchorMouseY;
    private static int hoveredSlot = -1;

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("skill_wheel", OVERLAY);
    }

    public static void begin() {
        Minecraft minecraft = Minecraft.getInstance();
        active = true;
        hoveredSlot = -1;
        anchorMouseX = scaledMouseX(minecraft);
        anchorMouseY = scaledMouseY(minecraft);
    }

    public static void update() {
        if (!active) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack scroll = ClientSelectedSkillData.findClientScroll();
        List<String> skills = SevenStarScrollHelper.getSkills(scroll);
        if (skills.isEmpty()) {
            hoveredSlot = -1;
            return;
        }
        double dx = scaledMouseX(minecraft) - anchorMouseX;
        double dy = scaledMouseY(minecraft) - anchorMouseY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < 14.0D) {
            hoveredSlot = -1;
            return;
        }
        double angle = Math.atan2(dy, dx) + Math.PI / 2.0D;
        if (angle < 0.0D) {
            angle += Math.PI * 2.0D;
        }
        hoveredSlot = Math.floorMod((int) Math.floor((angle + Math.PI / skills.size()) / (Math.PI * 2.0D / skills.size())), skills.size());
    }

    public static void finish() {
        if (!active) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && hoveredSlot >= 0) {
            ItemStack scroll = ClientSelectedSkillData.findClientScroll();
            String skillId = SevenStarScrollHelper.getSkill(scroll, hoveredSlot);
            if (skillId.isBlank()) {
                minecraft.player.displayClientMessage(Component.translatable("message.sevenstars.skill_slot_empty"), true);
            } else {
                ClientSelectedSkillData.setSelectedSlot(hoveredSlot);
                Component name = SkillEntryHelper.displayName(skillId);
                minecraft.player.displayClientMessage(Component.translatable("message.sevenstars.selected_spell", name), true);
            }
        }
        active = false;
        hoveredSlot = -1;
    }

    public static boolean isActive() {
        return active;
    }

    private static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!active) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack scroll = ClientSelectedSkillData.findClientScroll();
        List<String> skills = SevenStarScrollHelper.getSkills(scroll);
        if (minecraft.player == null || skills.isEmpty()) {
            return;
        }

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int radius = 74;
        drawCircleOutline(graphics, centerX, centerY, 92, 0xCCFFFFFF);
        drawCircleOutline(graphics, centerX, centerY, 54, 0x66FFFFFF);
        drawCircleOutline(graphics, centerX, centerY, 30, 0x99FFFFFF);

        Component centerText = Component.translatable("screen.sevenstars.no_wheel_selection");
        if (hoveredSlot >= 0) {
            String skillId = skills.get(hoveredSlot);
            centerText = skillId.isBlank()
                    ? Component.translatable("screen.sevenstars.empty_slot")
                    : SkillEntryHelper.displayName(skillId);
        }
        graphics.drawCenteredString(minecraft.font, centerText, centerX, centerY - 4, 0xFFFFFF);

        for (int slot = 0; slot < skills.size(); slot++) {
            double angle = -Math.PI / 2.0D + slot * (Math.PI * 2.0D / skills.size());
            int x = centerX + (int) Math.round(Math.cos(angle) * radius);
            int y = centerY + (int) Math.round(Math.sin(angle) * radius);
            boolean hovered = slot == hoveredSlot;
            int border = hovered ? 0xFFFFE080 : 0x99FFFFFF;
            int inner = hovered ? 0x55FFE080 : 0x33000000;
            drawSlotOutline(graphics, x, y, 58, 48, border);
            if (hovered) {
                drawLine(graphics, centerX, centerY, x, y, 0x88FFE080);
                graphics.fill(x - 25, y - 19, x + 25, y + 21, inner);
            }

            String skillId = skills.get(slot);
            if (skillId.isBlank()) {
                graphics.drawCenteredString(minecraft.font, Component.translatable("screen.sevenstars.empty_slot"), x, y - 4, 0x999999);
                continue;
            }
            SkillEntryHelper.parseSeriesEntry(skillId).ifPresentOrElse(series -> {
                SkillRegistry.getBySeriesRank(series, com.example.examplemod.skill.SkillRank.NORMAL).ifPresent(skill -> {
                    graphics.renderItem(skill.icon(), x - 8, y - 16);
                    graphics.drawCenteredString(minecraft.font, shortName(SkillEntryHelper.seriesDisplayName(series).getString()), x, y + 5, hovered ? 0xFFFFFF : 0xDDDDDD);
                });
            }, () -> SkillRegistry.get(skillId).ifPresent(skill -> {
                    graphics.renderItem(skill.icon(), x - 8, y - 16);
                    graphics.drawCenteredString(minecraft.font, shortName(skill.displayName().getString()), x, y + 5, hovered ? 0xFFFFFF : 0xDDDDDD);
                }));
        }
    }

    private static Component shortName(String name) {
        return Component.literal(name.length() > 8 ? name.substring(0, 8) : name);
    }

    private static void drawCircleOutline(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
        int previousX = centerX + radius;
        int previousY = centerY;
        for (int degree = 3; degree <= 360; degree += 3) {
            double radians = Math.toRadians(degree);
            int x = centerX + (int) Math.round(Math.cos(radians) * radius);
            int y = centerY + (int) Math.round(Math.sin(radians) * radius);
            drawLine(graphics, previousX, previousY, x, y, color);
            previousX = x;
            previousY = y;
        }
    }

    private static void drawSlotOutline(GuiGraphics graphics, int centerX, int centerY, int width, int height, int color) {
        int left = centerX - width / 2;
        int top = centerY - height / 2;
        int right = left + width;
        int bottom = top + height;
        graphics.fill(left, top, right, top + 1, color);
        graphics.fill(left, bottom - 1, right, bottom, color);
        graphics.fill(left, top, left + 1, bottom, color);
        graphics.fill(right - 1, top, right, bottom, color);
    }

    private static void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        if (steps == 0) {
            graphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            return;
        }
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private static double scaledMouseX(Minecraft minecraft) {
        return minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
    }

    private static double scaledMouseY(Minecraft minecraft) {
        return minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
    }
}
