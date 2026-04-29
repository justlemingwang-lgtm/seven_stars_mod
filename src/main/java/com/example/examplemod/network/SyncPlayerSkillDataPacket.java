package com.example.examplemod.network;

import com.example.examplemod.client.ClientSkillData;
import com.example.examplemod.skill.SkillSeries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public record SyncPlayerSkillDataPacket(Set<String> unlockedSkills, Set<SkillSeries> unlockedSeries) {
    public static void encode(SyncPlayerSkillDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.unlockedSkills.size());
        packet.unlockedSkills.forEach(buffer::writeUtf);
        buffer.writeVarInt(packet.unlockedSeries.size());
        packet.unlockedSeries.forEach(series -> buffer.writeEnum(series));
    }

    public static SyncPlayerSkillDataPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Set<String> skills = new HashSet<>();
        for (int i = 0; i < size; i++) {
            skills.add(buffer.readUtf());
        }
        int seriesSize = buffer.readVarInt();
        Set<SkillSeries> series = new HashSet<>();
        for (int i = 0; i < seriesSize; i++) {
            series.add(buffer.readEnum(SkillSeries.class));
        }
        return new SyncPlayerSkillDataPacket(skills, series);
    }

    public static void handle(SyncPlayerSkillDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientSkillData.setUnlocked(packet.unlockedSkills, packet.unlockedSeries));
        context.setPacketHandled(true);
    }
}
