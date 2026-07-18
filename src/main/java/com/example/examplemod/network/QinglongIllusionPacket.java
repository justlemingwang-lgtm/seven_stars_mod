package com.example.examplemod.network;

import com.example.examplemod.client.ClientQinglongIllusion;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record QinglongIllusionPacket(boolean active, int bossEntityId) {
    public static void encode(QinglongIllusionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
        buffer.writeVarInt(packet.bossEntityId);
    }

    public static QinglongIllusionPacket decode(FriendlyByteBuf buffer) {
        return new QinglongIllusionPacket(buffer.readBoolean(), buffer.readVarInt());
    }

    public static void handle(QinglongIllusionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientQinglongIllusion.set(packet.active, packet.bossEntityId));
        context.setPacketHandled(true);
    }
}
