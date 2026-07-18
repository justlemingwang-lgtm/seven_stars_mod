package com.example.examplemod.event;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.codex.CodexProgress;
import com.example.examplemod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class CodexEvents {
    private CodexEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CodexProgress.giveInitialBook(player);
            CodexProgress.sync(player, false);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CodexProgress.sync(player, false);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CodexProgress.sync(player, false);
        }
    }

    @SubscribeEvent
    public static void onItemFrameInteraction(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof ItemFrame frame)
                || !frame.getItem().is(ModItems.AZURE_DRAGON_SCALE.get())) {
            return;
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player) {
            boolean newlyUnlocked = CodexProgress.unlock(player, CodexProgress.AZURE_DRAGON);
            player.displayClientMessage(Component.translatable(newlyUnlocked
                    ? "message.sevenstars.codex.azure_dragon_unlocked"
                    : "message.sevenstars.codex.azure_dragon_known"), true);
            CodexProgress.sync(player, false);
        }
    }
}
