package com.example.examplemod.codex;

import com.example.examplemod.network.ModNetwork;
import com.example.examplemod.network.SyncCodexProgressPacket;
import com.example.examplemod.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.util.LinkedHashSet;
import java.util.Set;

public final class CodexProgress {
    public static final String AZURE_DRAGON = "stage3.azure_dragon";

    private static final String ROOT_TAG = "SevenStarsCodex";
    private static final String UNLOCKED_TAG = "UnlockedChapters";
    private static final String RECEIVED_BOOK_TAG = "ReceivedBook";
    private static final Set<String> DEFAULT_CHAPTERS = Set.of(
            "stage0.introduction", "stage0.qi", "stage0.spellcasting", "stage0.basic_spells",
            "stage1.journey", "stage1.pegasus", "stage1.ice_dipper", "stage1.hound_claw",
            "stage1.completion", "stage2.overview", "stage2.goat_horn", "stage2.goat_temple",
            "stage2.chop", "stage2.triangle_town", "stage2.triangle"
    );

    private CodexProgress() {
    }

    public static Set<String> getUnlocked(ServerPlayer player) {
        Set<String> result = new LinkedHashSet<>(DEFAULT_CHAPTERS);
        ListTag stored = root(player).getList(UNLOCKED_TAG, Tag.TAG_STRING);
        for (int index = 0; index < stored.size(); index++) {
            result.add(stored.getString(index));
        }
        return result;
    }

    public static boolean unlock(ServerPlayer player, String chapterId) {
        Set<String> unlocked = getUnlocked(player);
        if (!unlocked.add(chapterId)) {
            return false;
        }
        ListTag stored = new ListTag();
        unlocked.stream().filter(id -> !DEFAULT_CHAPTERS.contains(id))
                .forEach(id -> stored.add(StringTag.valueOf(id)));
        root(player).put(UNLOCKED_TAG, stored);
        return true;
    }

    public static void sync(ServerPlayer player, boolean openScreen) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncCodexProgressPacket(getUnlocked(player), openScreen));
    }

    public static void giveInitialBook(ServerPlayer player) {
        CompoundTag root = root(player);
        if (root.getBoolean(RECEIVED_BOOK_TAG)) {
            return;
        }
        root.putBoolean(RECEIVED_BOOK_TAG, true);
        ItemStack book = new ItemStack(ModItems.SEVEN_STARS_CODEX.get());
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
    }

    private static CompoundTag root(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        CompoundTag root = persisted.getCompound(ROOT_TAG);
        persisted.put(ROOT_TAG, root);
        return root;
    }
}
