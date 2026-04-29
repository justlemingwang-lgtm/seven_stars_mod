package com.example.examplemod.network;

import com.example.examplemod.client.ClientQiData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncSelfQiPacket(int qi, int maxQi) {
    public static void encode(SyncSelfQiPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.qi);
        buffer.writeInt(packet.maxQi);
    }

    public static SyncSelfQiPacket decode(FriendlyByteBuf buffer) {
        return new SyncSelfQiPacket(buffer.readInt(), buffer.readInt());
    }

    public static void handle(SyncSelfQiPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientQiData.setSelf(packet.qi, packet.maxQi));
        context.setPacketHandled(true);
    }
}
