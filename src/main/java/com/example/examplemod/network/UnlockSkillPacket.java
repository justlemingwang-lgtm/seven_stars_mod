package com.example.examplemod.network;

import com.example.examplemod.skill.SkillManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UnlockSkillPacket(String skillId) {
    public static void encode(UnlockSkillPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.skillId);
    }

    public static UnlockSkillPacket decode(FriendlyByteBuf buffer) {
        return new UnlockSkillPacket(buffer.readUtf());
    }

    public static void handle(UnlockSkillPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                SkillManager.unlock(sender, packet.skillId);
            }
        });
        context.setPacketHandled(true);
    }
}
