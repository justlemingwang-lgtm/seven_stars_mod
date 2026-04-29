package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.stage2.SkillIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class QiHudOverlay {
    private static final IGuiOverlay OVERLAY = QiHudOverlay::render;

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("qi", OVERLAY);
    }

    private static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        int x = 10;
        int y = screenHeight - 58;
        graphics.drawString(minecraft.font,
                Component.translatable("hud.sevenstars.qi", ClientQiData.getSelfQi(), ClientQiData.getSelfMaxQi()),
                x, y, 0x66DDFF, true);
        graphics.drawString(minecraft.font,
                Component.translatable("hud.sevenstars.current_spell", ClientSelectedSkillData.getDisplayName()),
                x, y - 12, 0xFFE680, true);
        graphics.drawString(minecraft.font,
                Component.translatable("hud.sevenstars.current_rank", ClientSelectedSkillData.getRankDisplayName()),
                x, y - 24, 0xFFCC99, true);

        HitResult hitResult = minecraft.hitResult;
        if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity living
                && ClientQiData.hasTarget(living.getId())) {
            graphics.drawString(minecraft.font,
                    Component.translatable("hud.sevenstars.target_qi", ClientQiData.getTargetQi(), ClientQiData.getTargetMaxQi()),
                    screenWidth / 2 + 12, screenHeight / 2 + 12, 0xFFDD66, true);
        }
        renderSkillStates(graphics, minecraft, screenWidth, screenHeight);
    }

    private static void renderSkillStates(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight) {
        int y = screenHeight - 72;
        y = drawDisabled(graphics, minecraft, screenWidth, y, SkillIds.GOAT_HORN, Component.translatable("skill.sevenstars.goat_horn"));
        y = drawCooldown(graphics, minecraft, screenWidth, y, SkillIds.GOAT_HORN, Component.translatable("skill.sevenstars.goat_horn"));
        int goatHornDisabled = ClientSkillStateData.getDisabledTicks(SkillIds.GOAT_HORN);
        if (goatHornDisabled > 0) {
            y = drawLine(graphics, minecraft, screenWidth, y,
                    Component.translatable("hud.sevenstars.skill_unavailable", Component.translatable("skill.sevenstars.goat_horn")));
        }
        y = drawCooldown(graphics, minecraft, screenWidth, y, SkillIds.CHOP, Component.translatable("skill.sevenstars.chop"));
        drawDisabled(graphics, minecraft, screenWidth, y, SkillIds.TRIANGLE, Component.translatable("skill.sevenstars.triangle"));
    }

    private static int drawCooldown(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int y, String skillId, Component name) {
        int ticks = ClientSkillStateData.getCooldownTicks(skillId);
        if (ticks <= 0) {
            return y;
        }
        return drawLine(graphics, minecraft, screenWidth, y,
                Component.translatable("hud.sevenstars.skill_cd", name, formatSeconds(ticks)));
    }

    private static int drawDisabled(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int y, String skillId, Component name) {
        int ticks = ClientSkillStateData.getDisabledTicks(skillId);
        if (ticks <= 0) {
            return y;
        }
        return drawLine(graphics, minecraft, screenWidth, y,
                Component.translatable("hud.sevenstars.skill_disabled", name, formatSeconds(ticks)));
    }

    private static int drawLine(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int y, Component text) {
        int width = minecraft.font.width(text);
        graphics.drawString(minecraft.font, text, screenWidth - width - 10, y, 0xFFE0AA, true);
        return y - 12;
    }

    private static String formatSeconds(int ticks) {
        return String.format(java.util.Locale.ROOT, "%.1f", ticks / 20.0D);
    }
}
