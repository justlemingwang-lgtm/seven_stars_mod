package com.example.examplemod.network;

import com.example.examplemod.client.ClientArmorDisableData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncArmorDisablePacket(int remainingTicks) {
    public static void encode(SyncArmorDisablePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.remainingTicks);
    }

    public static SyncArmorDisablePacket decode(FriendlyByteBuf buffer) {
        return new SyncArmorDisablePacket(buffer.readVarInt());
    }

    public static void handle(SyncArmorDisablePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientArmorDisableData.setRemainingTicks(packet.remainingTicks));
        context.setPacketHandled(true);
    }
}
