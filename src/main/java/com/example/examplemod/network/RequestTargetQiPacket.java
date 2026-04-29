package com.example.examplemod.network;

import com.example.examplemod.qi.QiManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RequestTargetQiPacket(int entityId) {
    public static void encode(RequestTargetQiPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
    }

    public static RequestTargetQiPacket decode(FriendlyByteBuf buffer) {
        return new RequestTargetQiPacket(buffer.readInt());
    }

    public static void handle(RequestTargetQiPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) {
                return;
            }
            Entity entity = sender.level().getEntity(packet.entityId);
            if (entity instanceof LivingEntity living && sender.distanceToSqr(living) <= 100.0D) {
                QiManager.syncTarget(sender, living);
            }
        });
        context.setPacketHandled(true);
    }
}
