package com.example.examplemod.stage3;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class ArmorDisableManager {
    private static final String TAG = "sevenstars_armor_disabled_ticks";
    private static final UUID LEGACY_ARMOR_UUID = UUID.fromString("87a9fbe9-78b9-4c81-9e4d-cc9a6cb8cb01");
    private static final UUID LEGACY_TOUGHNESS_UUID = UUID.fromString("87a9fbe9-78b9-4c81-9e4d-cc9a6cb8cb02");

    private ArmorDisableManager() {
    }

    public static void disableArmor(ServerPlayer player, int ticks) {
        if (ticks <= 0) return;
        int previous = getRemainingTicks(player);
        player.getPersistentData().putInt(TAG, Math.max(previous, ticks));
        removeLegacyGlobalModifiers(player);
        if (previous <= 0) player.displayClientMessage(Component.translatable("message.sevenstars.armor_disabled"), true);
    }

    public static boolean isArmorDisabled(Player player) {
        return getRemainingTicks(player) > 0;
    }

    public static int getRemainingTicks(Player player) {
        return player.getPersistentData().getInt(TAG);
    }

    public static void tick(Player player) {
        if (player.level().isClientSide()) return;
        removeLegacyGlobalModifiers(player);
        int remaining = getRemainingTicks(player);
        if (remaining <= 0) return;
        remaining--;
        if (remaining <= 0) clear(player);
        else player.getPersistentData().putInt(TAG, remaining);
    }

    public static void clear(Player player) {
        player.getPersistentData().remove(TAG);
        removeLegacyGlobalModifiers(player);
    }

    private static void removeLegacyGlobalModifiers(Player player) {
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        AttributeInstance toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armor != null) armor.removeModifier(LEGACY_ARMOR_UUID);
        if (toughness != null) toughness.removeModifier(LEGACY_TOUGHNESS_UUID);
    }
}
