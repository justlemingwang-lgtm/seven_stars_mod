package com.example.examplemod.network;

import com.example.examplemod.client.CodexClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public record SyncCodexProgressPacket(Set<String> unlockedChapters, boolean openScreen) {
    public static void encode(SyncCodexProgressPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.unlockedChapters.size());
        packet.unlockedChapters.forEach(buffer::writeUtf);
        buffer.writeBoolean(packet.openScreen);
    }

    public static SyncCodexProgressPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Set<String> chapters = new LinkedHashSet<>();
        for (int index = 0; index < size; index++) {
            chapters.add(buffer.readUtf(128));
        }
        return new SyncCodexProgressPacket(chapters, buffer.readBoolean());
    }

    public static void handle(SyncCodexProgressPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> CodexClientHooks.receive(packet.unlockedChapters, packet.openScreen));
        context.setPacketHandled(true);
    }
}
