package com.example.examplemod.network;

import com.example.examplemod.skill.SkillManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RemoveSkillFromScrollPacket(InteractionHand hand, int slot) {
    public static void encode(RemoveSkillFromScrollPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.hand);
        buffer.writeVarInt(packet.slot);
    }

    public static RemoveSkillFromScrollPacket decode(FriendlyByteBuf buffer) {
        return new RemoveSkillFromScrollPacket(buffer.readEnum(InteractionHand.class), buffer.readVarInt());
    }

    public static void handle(RemoveSkillFromScrollPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                SkillManager.removeSkill(sender, packet.hand, packet.slot);
            }
        });
        context.setPacketHandled(true);
    }
}
