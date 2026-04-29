package com.example.examplemod.network;

import com.example.examplemod.skill.SkillManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record WriteScrollPacket(String skillId, BlockPos tablePos) {
    public static void encode(WriteScrollPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.skillId);
        buffer.writeBlockPos(packet.tablePos);
    }

    public static WriteScrollPacket decode(FriendlyByteBuf buffer) {
        return new WriteScrollPacket(buffer.readUtf(), buffer.readBlockPos());
    }

    public static void handle(WriteScrollPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                SkillManager.writeSpellScroll(sender, packet.skillId, packet.tablePos);
            }
        });
        context.setPacketHandled(true);
    }
}
