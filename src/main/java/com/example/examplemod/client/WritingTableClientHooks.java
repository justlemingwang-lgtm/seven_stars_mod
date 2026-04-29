package com.example.examplemod.client;

import com.example.examplemod.client.gui.WritingTableScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class WritingTableClientHooks {
    public static void open(BlockPos pos) {
        Minecraft.getInstance().setScreen(new WritingTableScreen(pos));
    }
}
