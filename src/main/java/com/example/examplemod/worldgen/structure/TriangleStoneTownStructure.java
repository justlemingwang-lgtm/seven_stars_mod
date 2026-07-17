package com.example.examplemod.worldgen.structure;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.registry.ModStructures;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Connector-driven underground town generation modelled after vanilla strongholds.
 *
 * <p>Every room is created from an already placed room's jigsaw connector.  The
 * two connectors are opened only after the candidate passes collision, range and
 * build-height checks.  A rejected branch therefore leaves a solid wall instead
 * of an orphan door, and every accepted room is connected by construction.</p>
 */
public final class TriangleStoneTownStructure extends Structure {
    public static final Codec<TriangleStoneTownStructure> CODEC = simpleCodec(TriangleStoneTownStructure::new);

    private static final int MAX_DEPTH = 50;
    private static final int MAX_DISTANCE_FROM_START = 112;
    private static final int MAX_BOUND_DISTANCE_FROM_START = 128;
    private static final int MAX_PIECES = 72;
    private static final int MAX_LAYOUT_ATTEMPTS = 16;
    private static final int MIN_COMPLETE_LAYOUT_ROOMS = 24;
    private static final int CANDIDATE_ATTEMPTS = 5;
    private static final int TOWN_ENTRY_Y = 10;
    private static final int TOWN_MAX_Y = 19;
    private static final int ENTRANCE_SPIRAL_RADIUS = 7;

    private static final List<WeightedRoom> EXTENSION_ROOMS = List.of(
            new WeightedRoom("extension_long_corridor", 40),
            new WeightedRoom("extension_corridor_straight", 30),
            new WeightedRoom("extension_corridor_turn", 20),
            new WeightedRoom("extension_crossroads", 12),
            new WeightedRoom("extension_multilevel_hall", 10),
            new WeightedRoom("extension_library", 8),
            new WeightedRoom("extension_residential", 8),
            new WeightedRoom("extension_workshop", 7),
            new WeightedRoom("extension_guard", 7),
            new WeightedRoom("extension_brewing", 6),
            new WeightedRoom("extension_prison", 5),
            new WeightedRoom("extension_research", 5),
            new WeightedRoom("extension_observation", 4),
            new WeightedRoom("extension_core_room", 3),
            new WeightedRoom("extension_vertical_descent", 8),
            new WeightedRoom("extension_vertical_ascent", 6),
            new WeightedRoom("extension_lamp_shrine", 2)
    );

    public TriangleStoneTownStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        if (surfaceY <= TOWN_ENTRY_Y + 4) {
            return Optional.empty();
        }
        BlockPos anchor = new BlockPos(x, surfaceY, z);
        return Optional.of(new GenerationStub(anchor, builder -> generateTown(builder,
                context.structureTemplateManager(), context.random(), anchor,
                context.heightAccessor().getMinBuildHeight(), context.heightAccessor().getMaxBuildHeight())));
    }

    /** Vanilla strongholds retry their layout until the portal branch exists; this does the same for the core vault. */
    private static void generateTown(StructurePiecesBuilder builder, StructureTemplateManager templates,
                                     RandomSource random, BlockPos anchor, int minBuildHeight, int maxBuildHeight) {
        List<StructurePiece> bestLayout = List.of();
        int bestScore = Integer.MIN_VALUE;

        for (int attempt = 0; attempt < MAX_LAYOUT_ATTEMPTS; attempt++) {
            builder.clear();
            LayoutResult result = generateLayout(builder, templates, random, anchor, minBuildHeight, maxBuildHeight);
            int score = result.roomCount() + (result.hasCoreVault() ? MAX_PIECES : 0);
            if (score > bestScore) {
                bestScore = score;
                bestLayout = new ArrayList<>(builder.build().pieces());
            }
            if (result.hasCoreVault() && result.roomCount() >= MIN_COMPLETE_LAYOUT_ROOMS) {
                return;
            }
        }

        // A hostile modded world height or an unusually collision-heavy seed can exhaust retries.
        // Preserve the best connected attempt; it still cannot contain an isolated room.
        builder.clear();
        bestLayout.forEach(builder::addPiece);
    }

    private static LayoutResult generateLayout(StructurePiecesBuilder builder, StructureTemplateManager templates,
                                               RandomSource random, BlockPos anchor,
                                               int minBuildHeight, int maxBuildHeight) {
        TriangleStoneTownPiece start = new TriangleStoneTownPiece(templates, "entrance",
                anchor.offset(-15, -1, -15), Rotation.NONE, 0);
        builder.addPiece(start);

        BlockPos surfaceDoor = anchor.offset(0, 0, -15);
        start.openDoor(surfaceDoor, Direction.NORTH);

        BoundingBox spiralBounds = new BoundingBox(
                anchor.getX() - ENTRANCE_SPIRAL_RADIUS,
                TOWN_ENTRY_Y - 1,
                anchor.getZ() - 30,
                anchor.getX() + ENTRANCE_SPIRAL_RADIUS,
                anchor.getY() + 3,
                anchor.getZ() - 16);
        TriangleStoneTownPassagePiece entranceSpiral = new TriangleStoneTownPassagePiece(
                spiralBounds, TriangleStoneTownPassagePiece.Mode.ENTRANCE_SPIRAL, 1);
        builder.addPiece(entranceSpiral);

        TriangleStoneTownPiece townSquare = new TriangleStoneTownPiece(templates, "town_square",
                new BlockPos(anchor.getX() - 15, TOWN_ENTRY_Y - 1, anchor.getZ() - 61),
                Rotation.NONE, 2);
        townSquare.openDoor(new BlockPos(anchor.getX(), TOWN_ENTRY_Y, anchor.getZ() - 31), Direction.SOUTH);
        builder.addPiece(townSquare);

        List<PendingConnector> pending = collectOutputs(townSquare, 2);
        boolean hasCoreVault = false;
        int roomCount = 3;
        BlockPos layoutCenter = townSquare.getBoundingBox().getCenter();
        int townBuildCeiling = Math.min(maxBuildHeight, TOWN_MAX_Y + 6);

        // Stronghold pieces are expanded in random pending-child order rather than breadth-first order.
        while (!pending.isEmpty() && roomCount < MAX_PIECES) {
            PendingConnector opening = pending.remove(random.nextInt(pending.size()));
            if (opening.depth() >= MAX_DEPTH) {
                continue;
            }

            TriangleStoneTownPiece child = attachChild(builder, templates, random, opening,
                    layoutCenter, minBuildHeight, townBuildCeiling);
            if (child == null) {
                // The template connector remains triangle stone bricks when no child can be fitted.
                continue;
            }

            builder.addPiece(child);
            roomCount++;
            hasCoreVault |= child.assetName().equals("core_vault");
            pending.addAll(collectOutputs(child, opening.depth() + 1));
        }

        return new LayoutResult(roomCount, hasCoreVault);
    }

    private static TriangleStoneTownPiece attachChild(StructurePiecesBuilder builder,
                                                       StructureTemplateManager templates,
                                                       RandomSource random, PendingConnector opening,
                                                       BlockPos startCenter,
                                                       int minBuildHeight, int maxBuildHeight) {
        CompoundTag parentNbt = opening.connector().nbt();
        if (parentNbt == null) {
            return null;
        }

        List<WeightedRoom> pool = roomsForPool(parentNbt.getString("pool"));
        if (pool.isEmpty()) {
            return null;
        }

        Direction parentFacing = JigsawBlock.getFrontFacing(opening.connector().state());
        BlockPos attachmentPosition = opening.connector().pos().relative(parentFacing);

        for (int attempt = 0; attempt < CANDIDATE_ATTEMPTS; attempt++) {
            WeightedRoom selected = chooseWeighted(pool, random, opening.parent().assetName());
            Rotation[] rotations = shuffledRotations(random);
            for (Rotation rotation : rotations) {
                List<StructureTemplate.StructureBlockInfo> entries = jigsawBlocks(
                        templates, selected.name(), BlockPos.ZERO, rotation);
                shuffle(entries, random);

                for (StructureTemplate.StructureBlockInfo entry : entries) {
                    if (entry.nbt() == null || !JigsawBlock.canAttach(opening.connector(), entry)) {
                        continue;
                    }

                    BlockPos templateOrigin = attachmentPosition.subtract(entry.pos());
                    TriangleStoneTownPiece candidate = TriangleStoneTownPiece.atTemplateOrigin(
                            templates, selected.name(), templateOrigin, rotation, opening.depth() + 1);
                    BoundingBox bounds = candidate.getBoundingBox();
                    if (!insideGenerationLimits(bounds, attachmentPosition, startCenter,
                            minBuildHeight, maxBuildHeight)
                            || builder.findCollisionPiece(bounds) != null) {
                        continue;
                    }

                    BlockPos childDoor = entry.pos().offset(templateOrigin);
                    Direction childFacing = JigsawBlock.getFrontFacing(entry.state());
                    if (!childDoor.equals(attachmentPosition)
                            || childFacing != parentFacing.getOpposite()) {
                        continue;
                    }

                    opening.parent().openDoor(opening.connector().pos(), parentFacing);
                    candidate.openDoor(childDoor, childFacing);
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean insideGenerationLimits(BoundingBox bounds, BlockPos attachment,
                                                  BlockPos startCenter,
                                                  int minBuildHeight, int maxBuildHeight) {
        return bounds.minY() >= minBuildHeight + 5
                && bounds.maxY() < maxBuildHeight - 5
                && Math.abs(attachment.getX() - startCenter.getX()) <= MAX_DISTANCE_FROM_START
                && Math.abs(attachment.getZ() - startCenter.getZ()) <= MAX_DISTANCE_FROM_START
                && bounds.minX() >= startCenter.getX() - MAX_BOUND_DISTANCE_FROM_START
                && bounds.maxX() <= startCenter.getX() + MAX_BOUND_DISTANCE_FROM_START
                && bounds.minZ() >= startCenter.getZ() - MAX_BOUND_DISTANCE_FROM_START
                && bounds.maxZ() <= startCenter.getZ() + MAX_BOUND_DISTANCE_FROM_START;
    }

    private static List<PendingConnector> collectOutputs(TriangleStoneTownPiece room, int depth) {
        List<PendingConnector> outputs = new ArrayList<>();
        for (StructureTemplate.StructureBlockInfo connector : room.jigsawBlocks()) {
            CompoundTag nbt = connector.nbt();
            if (nbt == null || isEmptyId(nbt.getString("pool")) || isEmptyId(nbt.getString("target"))) {
                continue;
            }
            outputs.add(new PendingConnector(room, connector, depth));
        }
        return outputs;
    }

    private static boolean isEmptyId(String id) {
        return id.isEmpty() || id.equals("minecraft:empty");
    }

    private static List<StructureTemplate.StructureBlockInfo> jigsawBlocks(StructureTemplateManager templates,
                                                                           String roomName,
                                                                           BlockPos origin,
                                                                           Rotation rotation) {
        return new ArrayList<>(templates.getOrCreate(TriangleStoneTownPiece.templateId(roomName))
                .filterBlocks(origin, new StructurePlaceSettings().setRotation(rotation), Blocks.JIGSAW, true));
    }

    private static Rotation[] shuffledRotations(RandomSource random) {
        Rotation[] rotations = Rotation.values().clone();
        for (int index = rotations.length - 1; index > 0; index--) {
            int selected = random.nextInt(index + 1);
            Rotation swap = rotations[index];
            rotations[index] = rotations[selected];
            rotations[selected] = swap;
        }
        return rotations;
    }

    private static <T> void shuffle(List<T> values, RandomSource random) {
        for (int index = values.size() - 1; index > 0; index--) {
            int selected = random.nextInt(index + 1);
            T swap = values.get(index);
            values.set(index, values.get(selected));
            values.set(selected, swap);
        }
    }

    /** Mirrors vanilla's weighted piece selection and avoids immediate repeats when alternatives exist. */
    private static WeightedRoom chooseWeighted(List<WeightedRoom> rooms, RandomSource random, String previousRoom) {
        WeightedRoom selected = chooseWeighted(rooms, random);
        if (rooms.size() > 1 && selected.name().equals(previousRoom)) {
            for (int retry = 0; retry < 3 && selected.name().equals(previousRoom); retry++) {
                selected = chooseWeighted(rooms, random);
            }
        }
        return selected;
    }

    private static WeightedRoom chooseWeighted(List<WeightedRoom> rooms, RandomSource random) {
        int total = rooms.stream().mapToInt(WeightedRoom::weight).sum();
        int value = random.nextInt(total);
        for (WeightedRoom room : rooms) {
            value -= room.weight();
            if (value < 0) {
                return room;
            }
        }
        return rooms.get(0);
    }

    private static List<WeightedRoom> roomsForPool(String poolName) {
        ResourceLocation pool = ResourceLocation.tryParse(poolName);
        if (pool == null || !pool.getNamespace().equals(ExampleMod.MODID)) {
            return List.of();
        }
        String prefix = "triangle_stone_town/";
        String path = pool.getPath();
        if (!path.startsWith(prefix)) {
            return List.of();
        }

        return switch (path.substring(prefix.length())) {
            case "entrance_descent_pool" -> rooms("entrance_descent");
            case "tutorial_pool" -> rooms("tutorial_lamp_room");
            case "town_square_pool" -> rooms("town_square");
            case "corridor_01_pool" -> rooms("corridor_01_straight");
            case "living_hub_pool" -> rooms("living_hub");
            case "library_pool" -> rooms("library");
            case "brewing_pool" -> rooms("brewing_room");
            case "residential_pool" -> rooms("residential_ruin");
            case "workshop_pool" -> rooms("maintenance_workshop");
            case "corridor_02_pool" -> rooms("corridor_02_turn");
            case "control_hub_pool" -> rooms("control_hub");
            case "prison_pool" -> rooms("prison");
            case "guard_pool" -> rooms("guard_station");
            case "research_pool" -> rooms("research_room");
            case "descent_pool" -> rooms("stairs_down", "stairs_spiral_down");
            case "lower_hub_pool" -> rooms("lower_hub");
            case "treasure_pool" -> rooms("treasure_room_01", "treasure_room_02",
                    "treasure_room_03", "treasure_room_04");
            case "observation_pool" -> rooms("observation_gallery");
            case "sealed_pool" -> rooms("sealed_array");
            case "core_vault_pool" -> rooms("core_vault");
            case "corridor_chest_pool" -> rooms("corridor_chest");
            case "lamp_shrine_pool" -> rooms("lamp_shrine_dead_end");
            case "extension_pool" -> EXTENSION_ROOMS;
            case "vertical_access_pool" -> rooms("vertical_access_corridor");
            case "vertical_district_pool" -> rooms("vertical_district");
            case "ossuary_pool" -> rooms("ossuary");
            case "ritual_classroom_pool" -> rooms("ritual_classroom");
            case "collapsed_cistern_pool" -> rooms("collapsed_cistern");
            case "wraith_barracks_pool" -> rooms("wraith_barracks");
            case "archive_annex_pool" -> rooms("archive_annex");
            case "forge_chamber_pool" -> rooms("forge_chamber");
            case "meditation_cells_pool" -> rooms("meditation_cells");
            case "map_room_pool" -> rooms("map_room");
            default -> List.of();
        };
    }

    private static List<WeightedRoom> rooms(String... names) {
        List<WeightedRoom> rooms = new ArrayList<>(names.length);
        for (String name : names) {
            rooms.add(new WeightedRoom(name, 1));
        }
        return List.copyOf(rooms);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.TRIANGLE_STONE_TOWN.get();
    }

    private record WeightedRoom(String name, int weight) {
    }

    private record PendingConnector(TriangleStoneTownPiece parent,
                                    StructureTemplate.StructureBlockInfo connector,
                                    int depth) {
    }

    private record LayoutResult(int roomCount, boolean hasCoreVault) {
    }
}
