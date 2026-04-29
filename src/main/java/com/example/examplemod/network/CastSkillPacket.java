package com.example.examplemod.network;

import com.example.examplemod.skill.SkillManager;
import com.example.examplemod.skill.SkillRank;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CastSkillPacket(int slot, SkillRank rank) {
    public static void encode(CastSkillPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.slot);
        buffer.writeEnum(packet.rank);
    }

    public static CastSkillPacket decode(FriendlyByteBuf buffer) {
        return new CastSkillPacket(buffer.readVarInt(), buffer.readEnum(SkillRank.class));
    }

    public static void handle(CastSkillPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                SkillManager.castSlot(sender, packet.slot, packet.rank);
            }
        });
        context.setPacketHandled(true);
    }
}
