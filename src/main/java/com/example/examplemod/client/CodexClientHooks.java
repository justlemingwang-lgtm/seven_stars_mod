package com.example.examplemod.client;

import com.example.examplemod.client.gui.SevenStarsCodexScreen;
import net.minecraft.client.Minecraft;

import java.util.Set;

public final class CodexClientHooks {
    private CodexClientHooks() {
    }

    public static void receive(Set<String> chapters, boolean openScreen) {
        ClientCodexData.setUnlocked(chapters);
        Minecraft minecraft = Minecraft.getInstance();
        if (openScreen) {
            minecraft.setScreen(new SevenStarsCodexScreen());
        } else if (minecraft.screen instanceof SevenStarsCodexScreen screen) {
            screen.refreshUnlocks();
        }
    }
}
