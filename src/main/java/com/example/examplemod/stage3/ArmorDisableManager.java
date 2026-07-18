package com.example.examplemod.stage3;

import com.example.examplemod.Config;
import com.example.examplemod.qi.QiManager;
import com.example.examplemod.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public final class ArmorDisableManager {
    private static final String TAG = "sevenstars_armor_disabled_ticks";
    private static final UUID ARMOR_UUID = UUID.fromString("87a9fbe9-78b9-4c81-9e4d-cc9a6cb8cb01");
    private static final UUID TOUGHNESS_UUID = UUID.fromString("87a9fbe9-78b9-4c81-9e4d-cc9a6cb8cb02");

    private ArmorDisableManager() {
    }

    public static void disableArmor(ServerPlayer player, int ticks) {
        if (ticks <= 0) return;
        int previous = getRemainingTicks(player);
        player.getPersistentData().putInt(TAG, Math.max(previous, ticks));
        applyModifiers(player);
        if (previous <= 0 && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.QI_HELMET.get())) {
            QiManager.setMaxQi(player, Config.getMaxQiDefaultInt(), false);
        }
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
        int remaining = getRemainingTicks(player);
        if (remaining <= 0) {
            removeModifiers(player);
            return;
        }
        applyModifiers(player);
        remaining--;
        if (remaining <= 0) clear(player);
        else player.getPersistentData().putInt(TAG, remaining);
    }

    public static void clear(Player player) {
        player.getPersistentData().remove(TAG);
        removeModifiers(player);
        if (!player.level().isClientSide() && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.QI_HELMET.get())) {
            QiManager.setMaxQi(player, Config.qiHelmetMaxQi, false);
        }
    }

    private static void applyModifiers(Player player) {
        apply(player.getAttribute(Attributes.ARMOR), ARMOR_UUID, "Seven Stars armor disable");
        apply(player.getAttribute(Attributes.ARMOR_TOUGHNESS), TOUGHNESS_UUID, "Seven Stars toughness disable");
    }

    private static void apply(AttributeInstance attribute, UUID uuid, String name) {
        if (attribute == null || attribute.getModifier(uuid) != null) return;
        attribute.addTransientModifier(new AttributeModifier(uuid, name, -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void removeModifiers(Player player) {
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        AttributeInstance toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armor != null) armor.removeModifier(ARMOR_UUID);
        if (toughness != null) toughness.removeModifier(TOUGHNESS_UUID);
    }
}
