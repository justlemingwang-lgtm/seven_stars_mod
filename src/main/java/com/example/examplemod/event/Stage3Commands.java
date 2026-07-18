package com.example.examplemod.event;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.AzureSealChainBlock;
import com.example.examplemod.block.AzureSoulContainerBlockEntity;
import com.example.examplemod.entity.AzureDragonEntity;
import com.example.examplemod.item.AzureDragonEyeItem;
import com.example.examplemod.registry.ModBlocks;
import com.example.examplemod.stage3.AzureDragonAttackType;
import com.example.examplemod.stage3.Stage3Constants;
import com.example.examplemod.worldgen.structure.SkyArenaStructure;
import com.example.examplemod.worldgen.structure.SkyArenaStructurePiece;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
        var root = Commands.literal("sevenstars").requires(source -> source.hasPermission(2));
        root.then(Commands.literal("qinglong")
                .then(Commands.literal("status").executes(context -> {
                    AzureDragonEntity dragon = nearest(context.getSource().getEntityOrException().getX(),
                            context.getSource().getEntityOrException().getY(),
                            context.getSource().getEntityOrException().getZ(),
                            context.getSource().getLevel().getEntitiesOfClass(AzureDragonEntity.class,
                                    context.getSource().getEntityOrException().getBoundingBox().inflate(128.0D)));
                    if (dragon == null) return 0;
                    context.getSource().sendSuccess(() -> Component.literal("phase=" + dragon.getPhase()
                            + " attack=" + dragon.getAttackType() + " tick=" + dragon.getAttackTick()
                            + " transitioning=" + dragon.isTransitioning() + " defeated=" + dragon.isDefeated()), false);
                    return 1;
                }))
                .then(Commands.literal("phase")
                        .then(Commands.argument("phase", IntegerArgumentType.integer(2, 3))
                                .executes(context -> {
                                    AzureDragonEntity dragon = nearestDragon(context.getSource());
                                    if (dragon == null) return 0;
                                    dragon.beginPhaseTransition(IntegerArgumentType.getInteger(context, "phase"));
                                    return 1;
                                })))
                .then(Commands.literal("attack")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                        java.util.Arrays.stream(AzureDragonAttackType.values())
                                                .map(value -> value.name().toLowerCase()), builder))
                                .executes(context -> {
                                    AzureDragonEntity dragon = nearestDragon(context.getSource());
                                    if (dragon == null) return 0;
                                    try {
                                        dragon.startAttack(AzureDragonAttackType.valueOf(
                                                StringArgumentType.getString(context, "type").toUpperCase()));
                                        return 1;
                                    } catch (IllegalArgumentException ignored) {
                                        return 0;
                                    }
                                }))));

        root.then(Commands.literal("sky_arena")
                .then(Commands.literal("place")
                        .executes(context -> placeSkyArena(context.getSource(), 216))
                        .then(Commands.argument("floor_y", IntegerArgumentType.integer(
                                        SkyArenaStructure.MIN_FLOOR_Y, SkyArenaStructure.MAX_FLOOR_Y))
                                .executes(context -> placeSkyArena(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "floor_y")))))
                .then(Commands.literal("info").executes(context -> skyArenaInfo(context.getSource())))
                .then(Commands.literal("locate").executes(context -> locateSkyArena(context.getSource())))
                .then(Commands.literal("reset").executes(context -> resetSkyArena(context.getSource()))));
        event.getDispatcher().register(root);
    }

    private static int placeSkyArena(CommandSourceStack source, int floorY) {
        BlockPos center = BlockPos.containing(source.getPosition()).atY(floorY);
        int originY = floorY - SkyArenaStructure.FLOOR_LEVEL_IN_TEMPLATE;
        int placed = 0;
        for (SkyArenaStructure.TemplatePlacement template : SkyArenaStructure.templates()) {
            BlockPos origin = new BlockPos(center.getX() + template.offsetX(), originY,
                    center.getZ() + template.offsetZ());
            if (SkyArenaStructurePiece.placeDirect(source.getLevel(), template.name(), origin)) placed++;
        }
        int placedCount = placed;
        source.sendSuccess(() -> Component.literal("Sky arena templates placed=" + placedCount
                + "/9 center=" + center.toShortString() + " floorY=" + floorY
                + " highestY=" + SkyArenaStructure.highestWorldY(floorY)), true);
        skyArenaInfoAt(source, center);
        return placed == 9 ? 1 : 0;
    }

    private static int skyArenaInfo(CommandSourceStack source) {
        AzureSoulContainerBlockEntity container = nearestContainer(source, 96);
        if (container == null) {
            source.sendFailure(Component.literal("No loaded Azure soul container found within 96 blocks"));
            return 0;
        }
        BlockPos containerPos = container.getBlockPos();
        return skyArenaInfoAt(source, new BlockPos(containerPos.getX(), containerPos.getY() - 1,
                containerPos.getZ()));
    }

    private static int skyArenaInfoAt(CommandSourceStack source, BlockPos center) {
        int floorY = center.getY();
        int originY = floorY - SkyArenaStructure.FLOOR_LEVEL_IN_TEMPLATE;
        source.sendSuccess(() -> Component.literal("Sky arena center=" + center.toShortString()
                + " radius=" + SkyArenaStructure.ARENA_RADIUS + " floorY=" + floorY
                + " highestY=" + SkyArenaStructure.highestWorldY(floorY)
                + " containers=1 seals=7"), false);
        for (SkyArenaStructure.TemplatePlacement template : SkyArenaStructure.templates()) {
            BlockPos minimum = new BlockPos(center.getX() + template.offsetX(), originY,
                    center.getZ() + template.offsetZ());
            BlockPos maximum = minimum.offset(template.sizeX() - 1, SkyArenaStructure.TEMPLATE_HEIGHT - 1,
                    template.sizeZ() - 1);
            source.sendSuccess(() -> Component.literal("  " + template.name() + " origin="
                    + minimum.toShortString() + " bounds=" + minimum.toShortString() + " -> "
                    + maximum.toShortString()), false);
        }
        return 1;
    }

    private static int locateSkyArena(CommandSourceStack source) {
        BlockPos origin = BlockPos.containing(source.getPosition());
        BlockPos found = source.getLevel().findNearestMapStructure(SkyArenaStructure.LOCATABLE_TAG, origin,
                AzureDragonEyeItem.SEARCH_RADIUS_CHUNKS, false);
        if (found == null) {
            source.sendFailure(Component.literal("No sevenstars:sky_arena found within the configured search radius"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Nearest sevenstars:sky_arena: " + found.toShortString()
                + " distance=" + Math.round(Math.sqrt(origin.distSqr(found)))), false);
        return 1;
    }

    private static int resetSkyArena(CommandSourceStack source) {
        AzureSoulContainerBlockEntity nearest = nearestContainer(source, 96);
        if (nearest == null) {
            source.sendFailure(Component.literal("No loaded Azure soul container found within 96 blocks"));
            return 0;
        }

        BlockPos center = nearest.getBlockPos();
        int resetSeals = 0;
        int radius = Stage3Constants.SEAL_SCAN_RADIUS;
        for (BlockPos scan : BlockPos.betweenClosed(center.offset(-radius, -radius / 2, -radius),
                center.offset(radius, radius / 2, radius))) {
            BlockState state = source.getLevel().getBlockState(scan);
            if (state.is(ModBlocks.AZURE_SEAL_CHAIN.get())) {
                source.getLevel().setBlock(scan, state.setValue(AzureSealChainBlock.BROKEN, false), Block.UPDATE_ALL);
                resetSeals++;
            }
        }
        nearest.resetForDebug();
        int finalResetSeals = resetSeals;
        source.sendSuccess(() -> Component.literal("Reset sky arena at " + center.toShortString()
                + ": seals=" + finalResetSeals + ", container summon state cleared"), true);
        return resetSeals == 7 ? 1 : 0;
    }

    private static AzureSoulContainerBlockEntity nearestContainer(CommandSourceStack source, int radius) {
        BlockPos origin = BlockPos.containing(source.getPosition());
        AzureSoulContainerBlockEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (BlockPos scan : BlockPos.betweenClosed(origin.offset(-radius, -32, -radius),
                origin.offset(radius, 32, radius))) {
            if (source.getLevel().getBlockEntity(scan) instanceof AzureSoulContainerBlockEntity container) {
                double distance = scan.distSqr(origin);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = container;
                }
            }
        }
        return nearest;
    }

    private static AzureDragonEntity nearestDragon(CommandSourceStack source) {
        try {
            var entity = source.getEntityOrException();
            return nearest(entity.getX(), entity.getY(), entity.getZ(), source.getLevel().getEntitiesOfClass(
                    AzureDragonEntity.class, entity.getBoundingBox().inflate(128.0D)));
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            return null;
        }
    }

    private static AzureDragonEntity nearest(double x, double y, double z,
                                              java.util.List<AzureDragonEntity> dragons) {
        return dragons.stream().min(Comparator.comparingDouble(dragon -> dragon.distanceToSqr(x, y, z)))
                .orElse(null);
    }
}
