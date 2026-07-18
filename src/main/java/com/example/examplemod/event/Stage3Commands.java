package com.example.examplemod.event;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.AzureDragonEntity;
import com.example.examplemod.stage3.AzureDragonAttackType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class Stage3Commands {
    private Stage3Commands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("sevenstars")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("qinglong")
                        .then(Commands.literal("status").executes(context -> {
                            AzureDragonEntity dragon = nearest(context.getSource().getEntityOrException().getX(),
                                    context.getSource().getEntityOrException().getY(), context.getSource().getEntityOrException().getZ(),
                                    context.getSource().getLevel().getEntitiesOfClass(AzureDragonEntity.class,
                                            context.getSource().getEntityOrException().getBoundingBox().inflate(128.0D)));
                            if (dragon == null) return 0;
                            context.getSource().sendSuccess(() -> Component.literal("phase=" + dragon.getPhase()
                                    + " attack=" + dragon.getAttackType() + " tick=" + dragon.getAttackTick()
                                    + " transitioning=" + dragon.isTransitioning() + " defeated=" + dragon.isDefeated()), false);
                            return 1;
                        }))
                        .then(Commands.literal("phase").then(Commands.argument("phase", IntegerArgumentType.integer(2, 3))
                                .executes(context -> {
                                    AzureDragonEntity dragon = nearestDragon(context.getSource());
                                    if (dragon == null) return 0;
                                    dragon.beginPhaseTransition(IntegerArgumentType.getInteger(context, "phase"));
                                    return 1;
                                })))
                        .then(Commands.literal("attack").then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                        java.util.Arrays.stream(AzureDragonAttackType.values()).map(value -> value.name().toLowerCase()), builder))
                                .executes(context -> {
                                    AzureDragonEntity dragon = nearestDragon(context.getSource());
                                    if (dragon == null) return 0;
                                    try {
                                        dragon.startAttack(AzureDragonAttackType.valueOf(StringArgumentType.getString(context, "type").toUpperCase()));
                                        return 1;
                                    } catch (IllegalArgumentException ignored) {
                                        return 0;
                                    }
                                })))));
    }

    private static AzureDragonEntity nearestDragon(net.minecraft.commands.CommandSourceStack source) {
        try {
            var entity = source.getEntityOrException();
            return nearest(entity.getX(), entity.getY(), entity.getZ(), source.getLevel().getEntitiesOfClass(
                    AzureDragonEntity.class, entity.getBoundingBox().inflate(128.0D)));
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            return null;
        }
    }

    private static AzureDragonEntity nearest(double x, double y, double z, java.util.List<AzureDragonEntity> dragons) {
        return dragons.stream().min(Comparator.comparingDouble(dragon -> dragon.distanceToSqr(x, y, z))).orElse(null);
    }
}
