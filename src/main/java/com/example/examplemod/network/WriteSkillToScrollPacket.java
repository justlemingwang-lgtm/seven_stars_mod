package com.example.examplemod.network;

import com.example.examplemod.skill.SkillManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record WriteSkillToScrollPacket(InteractionHand hand, int slot, String skillId) {
    public static void encode(WriteSkillToScrollPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.hand);
        buffer.writeVarInt(packet.slot);
        buffer.writeUtf(packet.skillId);
    }

    public static WriteSkillToScrollPacket decode(FriendlyByteBuf buffer) {
        return new WriteSkillToScrollPacket(buffer.readEnum(InteractionHand.class), buffer.readVarInt(), buffer.readUtf());
    }

    public static void handle(WriteSkillToScrollPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                SkillManager.writeSkill(sender, packet.hand, packet.slot, packet.skillId);
            }
        });
        context.setPacketHandled(true);
    }
}
