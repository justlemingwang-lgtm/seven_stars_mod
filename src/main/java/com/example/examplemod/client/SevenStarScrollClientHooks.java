package com.example.examplemod.client;

import com.example.examplemod.client.gui.SkillTreeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

public class SevenStarScrollClientHooks {
    public static void open(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new SkillTreeScreen(Component.translatable("screen.sevenstars.seven_star_scroll"), hand));
    }
}
