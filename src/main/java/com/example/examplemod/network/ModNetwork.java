package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId;

    public static void register() {
        CHANNEL.messageBuilder(SyncSelfQiPacket.class, nextId())
                .encoder(SyncSelfQiPacket::encode)
                .decoder(SyncSelfQiPacket::decode)
                .consumerMainThread(SyncSelfQiPacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncTargetQiPacket.class, nextId())
                .encoder(SyncTargetQiPacket::encode)
                .decoder(SyncTargetQiPacket::decode)
                .consumerMainThread(SyncTargetQiPacket::handle)
                .add();
        CHANNEL.messageBuilder(RequestTargetQiPacket.class, nextId())
                .encoder(RequestTargetQiPacket::encode)
                .decoder(RequestTargetQiPacket::decode)
                .consumerMainThread(RequestTargetQiPacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncPlayerSkillDataPacket.class, nextId())
                .encoder(SyncPlayerSkillDataPacket::encode)
                .decoder(SyncPlayerSkillDataPacket::decode)
                .consumerMainThread(SyncPlayerSkillDataPacket::handle)
                .add();
        CHANNEL.messageBuilder(UnlockSkillPacket.class, nextId())
                .encoder(UnlockSkillPacket::encode)
                .decoder(UnlockSkillPacket::decode)
                .consumerMainThread(UnlockSkillPacket::handle)
                .add();
        CHANNEL.messageBuilder(WriteSkillToScrollPacket.class, nextId())
                .encoder(WriteSkillToScrollPacket::encode)
                .decoder(WriteSkillToScrollPacket::decode)
                .consumerMainThread(WriteSkillToScrollPacket::handle)
                .add();
        CHANNEL.messageBuilder(RemoveSkillFromScrollPacket.class, nextId())
                .encoder(RemoveSkillFromScrollPacket::encode)
                .decoder(RemoveSkillFromScrollPacket::decode)
                .consumerMainThread(RemoveSkillFromScrollPacket::handle)
                .add();
        CHANNEL.messageBuilder(CastSkillPacket.class, nextId())
                .encoder(CastSkillPacket::encode)
                .decoder(CastSkillPacket::decode)
                .consumerMainThread(CastSkillPacket::handle)
                .add();
        CHANNEL.messageBuilder(WriteScrollPacket.class, nextId())
                .encoder(WriteScrollPacket::encode)
                .decoder(WriteScrollPacket::decode)
                .consumerMainThread(WriteScrollPacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncSkillStatePacket.class, nextId())
                .encoder(SyncSkillStatePacket::encode)
                .decoder(SyncSkillStatePacket::decode)
                .consumerMainThread(SyncSkillStatePacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncCodexProgressPacket.class, nextId())
                .encoder(SyncCodexProgressPacket::encode)
                .decoder(SyncCodexProgressPacket::decode)
                .consumerMainThread(SyncCodexProgressPacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncArmorDisablePacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncArmorDisablePacket::encode)
                .decoder(SyncArmorDisablePacket::decode)
                .consumerMainThread(SyncArmorDisablePacket::handle)
                .add();
        CHANNEL.messageBuilder(QinglongIllusionPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(QinglongIllusionPacket::encode)
                .decoder(QinglongIllusionPacket::decode)
                .consumerMainThread(QinglongIllusionPacket::handle)
                .add();
    }

    private static int nextId() {
        return packetId++;
    }
}
