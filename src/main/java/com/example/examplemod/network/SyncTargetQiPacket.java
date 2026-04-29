package com.example.examplemod.network;

import com.example.examplemod.client.ClientQiData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncTargetQiPacket(int entityId, int qi, int maxQi) {
    public static void encode(SyncTargetQiPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
        buffer.writeInt(packet.qi);
        buffer.writeInt(packet.maxQi);
    }

    public static SyncTargetQiPacket decode(FriendlyByteBuf buffer) {
        return new SyncTargetQiPacket(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(SyncTargetQiPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientQiData.setTarget(packet.entityId, packet.qi, packet.maxQi));
        context.setPacketHandled(true);
    }
}
