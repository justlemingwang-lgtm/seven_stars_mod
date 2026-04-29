package com.example.examplemod.network;

import com.example.examplemod.client.ClientSkillStateData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record SyncSkillStatePacket(Map<String, Integer> cooldowns, Map<String, Integer> disabled) {
    public static void encode(SyncSkillStatePacket packet, FriendlyByteBuf buffer) {
        writeMap(buffer, packet.cooldowns);
        writeMap(buffer, packet.disabled);
    }

    public static SyncSkillStatePacket decode(FriendlyByteBuf buffer) {
        return new SyncSkillStatePacket(readMap(buffer), readMap(buffer));
    }

    public static void handle(SyncSkillStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientSkillStateData.set(packet.cooldowns, packet.disabled));
        context.setPacketHandled(true);
    }

    private static void writeMap(FriendlyByteBuf buffer, Map<String, Integer> map) {
        buffer.writeVarInt(map.size());
        map.forEach((skillId, ticks) -> {
            buffer.writeUtf(skillId);
            buffer.writeVarInt(ticks);
        });
    }

    private static Map<String, Integer> readMap(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(buffer.readUtf(), buffer.readVarInt());
        }
        return map;
    }
}
