package com.example.examplemod.worldgen.structure;

import com.example.examplemod.registry.ModStructures;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A stronghold-style structure graph with a guaranteed core and weighted repeatable wings.
 * Unlike a jigsaw structure, authored rooms are never lost to a seven-level recursion cap.
 */
public final class TriangleStoneTownStructure extends Structure {
    public static final Codec<TriangleStoneTownStructure> CODEC = simpleCodec(TriangleStoneTownStructure::new);

    private static final List<WeightedRoom> REPEATABLE_ROOMS = List.of(
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
        BlockPos anchor = new BlockPos(x, surfaceY, z);
        return Optional.of(new GenerationStub(anchor,
                builder -> generateTown(builder, context.structureTemplateManager(), context.random(), anchor,
                        context.heightAccessor().getMinBuildHeight())));
    }

    private static void generateTown(StructurePiecesBuilder builder, StructureTemplateManager templates,
                                     RandomSource random, BlockPos anchor, int minBuildHeight) {
        int ox = anchor.getX();
        int oz = anchor.getZ();
        int surface = anchor.getY();
        int mainY = Math.max(minBuildHeight + 68, surface - 42);
        int lowerY = mainY - 22;
        int deepY = mainY - 62;
        int upperGalleryY = mainY + 13;

        // Compact in X/Z, expansive in Y: every piece remains inside vanilla's eight-chunk reference radius.
        room(builder, templates, "entrance", ox - 15, surface - 1, oz - 15);
        room(builder, templates, "entrance_descent", ox - 4, surface - 13, oz - 28);
        room(builder, templates, "tutorial_lamp_room", ox - 7, mainY, oz - 15);
        room(builder, templates, "town_square", ox - 15, mainY, oz - 55);
        room(builder, templates, "corridor_01_straight", ox + 20, mainY, oz - 48,
                Rotation.CLOCKWISE_90, 1);
        room(builder, templates, "living_hub", ox + 28, mainY, oz - 55);
        room(builder, templates, "library", ox + 28, mainY, oz - 103);
        room(builder, templates, "brewing_room", ox + 50, mainY, oz - 101);
        room(builder, templates, "residential_ruin", ox + 70, mainY, oz - 101);
        room(builder, templates, "maintenance_workshop", ox + 90, mainY, oz - 101);
        room(builder, templates, "corridor_02_turn", ox - 28, mainY, oz - 50);
        room(builder, templates, "control_hub", ox - 75, mainY, oz - 55);
        room(builder, templates, "research_room", ox - 108, mainY, oz - 104);
        room(builder, templates, "prison", ox - 84, mainY, oz - 103);
        room(builder, templates, "guard_station", ox - 59, mainY, oz - 101);

        room(builder, templates, "stairs_down", ox - 20, mainY - 10, oz - 84);
        room(builder, templates, "stairs_spiral_down", ox + 14, mainY - 10, oz - 84);
        room(builder, templates, "lower_hub", ox - 23, lowerY, oz - 58);
        room(builder, templates, "treasure_room_01", ox - 50, lowerY, oz - 56);
        room(builder, templates, "treasure_room_02", ox - 50, lowerY, oz - 101);
        room(builder, templates, "treasure_room_03", ox + 25, lowerY, oz - 56);
        room(builder, templates, "treasure_room_04", ox + 25, lowerY, oz - 101);
        room(builder, templates, "observation_gallery", ox + 15, lowerY, oz - 118);
        room(builder, templates, "sealed_array", ox - 11, lowerY, oz - 88);
        room(builder, templates, "core_vault", ox - 8, lowerY, oz - 120);
        room(builder, templates, "corridor_chest", ox + 57, lowerY, oz - 91);
        room(builder, templates, "lamp_shrine_dead_end", ox + 85, lowerY, oz - 88);

        // The vertical district spans 47 blocks and has rooms opening at eight different elevations.
        room(builder, templates, "vertical_access_corridor", ox + 40, lowerY, oz - 62);
        room(builder, templates, "vertical_district", ox + 55, deepY, oz - 75);
        room(builder, templates, "ossuary", ox + 55, deepY + 1, oz - 100);
        room(builder, templates, "ritual_classroom", ox + 80, deepY + 13, oz - 102);
        room(builder, templates, "collapsed_cistern", ox + 25, deepY + 25, oz - 108);
        room(builder, templates, "wraith_barracks", ox + 103, deepY + 4, oz - 72);
        room(builder, templates, "archive_annex", ox + 103, deepY + 20, oz - 38);
        room(builder, templates, "forge_chamber", ox + 70, deepY + 1, oz - 25);
        room(builder, templates, "meditation_cells", ox + 35, deepY + 18, oz - 25);
        room(builder, templates, "map_room", ox + 27, deepY + 31, oz - 65);

        // Three authored straight stairs and three spiral stairs are guaranteed.
        room(builder, templates, "stairs_down", ox - 120, lowerY - 10, oz - 50,
                Rotation.CLOCKWISE_90, 9);
        room(builder, templates, "stairs_spiral_down", ox - 120, deepY + 10, oz - 18,
                Rotation.CLOCKWISE_90, 9);
        room(builder, templates, "stairs_down", ox + 110, deepY + 7, oz - 112,
                Rotation.COUNTERCLOCKWISE_90, 9);
        room(builder, templates, "stairs_spiral_down", ox + 108, deepY + 20, oz - 15,
                Rotation.CLOCKWISE_180, 9);

        room(builder, templates, "extension_long_corridor", ox - 105, lowerY, oz - 115);
        room(builder, templates, "extension_crossroads", ox - 105, deepY + 5, oz - 65);
        room(builder, templates, "extension_vertical_descent", ox - 65, mainY - 18, oz - 118);
        room(builder, templates, "extension_vertical_ascent", ox - 45, deepY + 2, oz - 95);
        room(builder, templates, "extension_multilevel_hall", ox - 105, deepY + 22, oz - 25);

        // The extension gallery is above the main floor, keeping the footprint large but place-command safe.
        String[] gallery = {
                "extension_library", "extension_brewing", "extension_residential",
                "extension_workshop", "extension_prison", "extension_guard",
                "extension_research", "extension_observation", "extension_core_room",
                "extension_lamp_shrine", "extension_corridor_straight", "extension_corridor_turn"
        };
        for (int i = 0; i < gallery.length; i++) {
            int row = i / 6;
            int column = i % 6;
            int x = ox - 112 + column * 40;
            int z = oz - 118 + row * 32;
            room(builder, templates, gallery[i], x, upperGalleryY, z,
                    column % 2 == 0 ? Rotation.NONE : Rotation.CLOCKWISE_180, 10 + i);
        }

        // Stronghold-like weighted repetition, now constrained to valid vanilla structure-reference cells.
        generateWeightedWings(builder, templates, random, ox, oz, lowerY, deepY);

        // Passages are last so their explicit AIR volume cuts doorways through every template wall.
        Map<String, List<TriangleStoneTownPiece>> rooms = indexRooms(builder);

        connect(builder, rooms, "tutorial_lamp_room", "town_square", 1);
        connect(builder, rooms, "town_square", "corridor_01_straight", 2);
        connect(builder, rooms, "corridor_01_straight", "living_hub", 2);
        connect(builder, rooms, "town_square", "corridor_02_turn", 2);
        connect(builder, rooms, "corridor_02_turn", "control_hub", 2);
        connect(builder, rooms, "living_hub", "library", 3);
        connect(builder, rooms, "library", "brewing_room", 3);
        connect(builder, rooms, "brewing_room", "residential_ruin", 3);
        connect(builder, rooms, "residential_ruin", "maintenance_workshop", 3);
        connect(builder, rooms, "control_hub", "guard_station", 3);
        connect(builder, rooms, "guard_station", "prison", 3);
        connect(builder, rooms, "prison", "research_room", 3);

        connect(builder, rooms, "lower_hub", "treasure_room_01", 5);
        connect(builder, rooms, "treasure_room_01", "treasure_room_02", 5);
        connect(builder, rooms, "lower_hub", "treasure_room_03", 5);
        connect(builder, rooms, "treasure_room_03", "treasure_room_04", 5);
        connect(builder, rooms, "lower_hub", "sealed_array", 5);
        connect(builder, rooms, "sealed_array", "core_vault", 5);
        connect(builder, rooms, "core_vault", "observation_gallery", 5);
        connect(builder, rooms, "lower_hub", "corridor_chest", 6);
        connect(builder, rooms, "corridor_chest", "lamp_shrine_dead_end", 6);
        connect(builder, rooms, "lower_hub", "vertical_access_corridor", 6);
        connect(builder, rooms, "vertical_access_corridor", "vertical_district", 6);

        connect(builder, rooms, "vertical_district", "ossuary", 8);
        connect(builder, rooms, "vertical_district", "ritual_classroom", 8);
        connect(builder, rooms, "vertical_district", "collapsed_cistern", 8);
        connect(builder, rooms, "vertical_district", "wraith_barracks", 8);
        connect(builder, rooms, "vertical_district", "archive_annex", 8);
        connect(builder, rooms, "vertical_district", "forge_chamber", 8);
        connect(builder, rooms, "vertical_district", "meditation_cells", 8);
        connect(builder, rooms, "vertical_district", "map_room", 8);

        for (int i = 0; i < gallery.length - 1; i++) {
            if (i != 5) {
                connect(builder, rooms, gallery[i], gallery[i + 1], 10);
            }
        }
        connect(builder, rooms, gallery[0], gallery[6], 10);

        stairZ(builder, ox, oz - 3, oz - 40, mainY + 1, surface + 1, 1);
        stairZ(builder, ox, oz - 55, oz - 14, lowerY + 1, mainY + 1, 4);
        stairX(builder, ox + 25, ox + 78, oz - 10, deepY + 3, lowerY + 1, 7);
        stairZ(builder, ox + 115, oz - 25, oz - 78, deepY + 3, lowerY + 1, 7);
        stairZ(builder, ox - 115, oz - 10, oz - 70, mainY + 1, upperGalleryY + 1, 9);
        hall(builder, ox - 115, oz - 70, ox - 72, oz - 76, mainY + 1, 9);
        hall(builder, ox - 115, oz - 10, ox - 102, oz - 108, upperGalleryY + 1, 10);
    }

    private static void generateWeightedWings(StructurePiecesBuilder builder, StructureTemplateManager templates,
                                              RandomSource random, int ox, int oz, int lowerY, int deepY) {
        int[] floors = {deepY, deepY + 14, lowerY, lowerY + 11};
        int added = 0;
        for (int attempt = 0; attempt < 36 && added < 10; attempt++) {
            WeightedRoom chosen = chooseWeighted(random);
            Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
            int x = ox - 105 + random.nextInt(6) * 35;
            int z = oz - 115 + random.nextInt(4) * 30;
            int y = floors[random.nextInt(floors.length)];
            TriangleStoneTownPiece candidate = new TriangleStoneTownPiece(templates, chosen.name(),
                    new BlockPos(x, y, z), rotation, 20 + added);
            if (builder.findCollisionPiece(candidate.getBoundingBox()) == null) {
                builder.addPiece(candidate);
                added++;
            }
        }
    }

    private static WeightedRoom chooseWeighted(RandomSource random) {
        int total = REPEATABLE_ROOMS.stream().mapToInt(WeightedRoom::weight).sum();
        int value = random.nextInt(total);
        for (WeightedRoom room : REPEATABLE_ROOMS) {
            value -= room.weight();
            if (value < 0) {
                return room;
            }
        }
        return REPEATABLE_ROOMS.get(0);
    }

    private static TriangleStoneTownPiece room(StructurePiecesBuilder builder, StructureTemplateManager templates,
                                               String name, int x, int y, int z) {
        return room(builder, templates, name, x, y, z, Rotation.NONE, 0);
    }

    private static TriangleStoneTownPiece room(StructurePiecesBuilder builder, StructureTemplateManager templates,
                                               String name, int x, int y, int z, Rotation rotation, int depth) {
        TriangleStoneTownPiece piece = new TriangleStoneTownPiece(templates, name,
                new BlockPos(x, y, z), rotation, depth);
        builder.addPiece(piece);
        return piece;
    }

    private static Map<String, List<TriangleStoneTownPiece>> indexRooms(StructurePiecesBuilder builder) {
        Map<String, List<TriangleStoneTownPiece>> rooms = new HashMap<>();
        for (var piece : builder.build().pieces()) {
            if (piece instanceof TriangleStoneTownPiece room) {
                rooms.computeIfAbsent(room.assetName(), ignored -> new ArrayList<>()).add(room);
            }
        }
        return rooms;
    }

    private static void connect(StructurePiecesBuilder builder,
                                Map<String, List<TriangleStoneTownPiece>> rooms,
                                String firstName, String secondName, int depth) {
        List<TriangleStoneTownPiece> first = rooms.get(firstName);
        List<TriangleStoneTownPiece> second = rooms.get(secondName);
        if (first == null || first.isEmpty() || second == null || second.isEmpty()) {
            return;
        }
        connect(builder, first.get(0).getBoundingBox(), second.get(0).getBoundingBox(), depth);
    }

    /** Selects real rotated wall faces, then cuts two blocks into both rooms to guarantee an open doorway. */
    private static void connect(StructurePiecesBuilder builder, BoundingBox a, BoundingBox b, int depth) {
        int floorY = Math.max(a.minY(), b.minY()) + 1;
        int highestUsableFloor = Math.min(a.maxY(), b.maxY()) - 3;
        if (floorY > highestUsableFloor) {
            return;
        }

        int xGap = axisGap(a.minX(), a.maxX(), b.minX(), b.maxX());
        int zGap = axisGap(a.minZ(), a.maxZ(), b.minZ(), b.maxZ());
        int aCenterX = (a.minX() + a.maxX()) / 2;
        int aCenterZ = (a.minZ() + a.maxZ()) / 2;
        int bCenterX = (b.minX() + b.maxX()) / 2;
        int bCenterZ = (b.minZ() + b.maxZ()) / 2;

        if (xGap == 0 && zGap == 0) {
            hall(builder, aCenterX, aCenterZ, bCenterX, bCenterZ, floorY, depth);
            return;
        }

        if (xGap > 0 && (zGap == 0 || xGap >= zGap)) {
            boolean towardPositiveX = bCenterX > aCenterX;
            int ax = towardPositiveX ? a.maxX() - 2 : a.minX() + 2;
            int bx = towardPositiveX ? b.minX() + 2 : b.maxX() - 2;
            int[] doorZ = facingCoordinates(a.minZ(), a.maxZ(), b.minZ(), b.maxZ(),
                    aCenterZ, bCenterZ);
            addHallX(builder, ax, bx, doorZ[0], floorY, depth);
            addHallZ(builder, bx, doorZ[0], doorZ[1], floorY, depth);
        } else {
            boolean towardPositiveZ = bCenterZ > aCenterZ;
            int az = towardPositiveZ ? a.maxZ() - 2 : a.minZ() + 2;
            int bz = towardPositiveZ ? b.minZ() + 2 : b.maxZ() - 2;
            int[] doorX = facingCoordinates(a.minX(), a.maxX(), b.minX(), b.maxX(),
                    aCenterX, bCenterX);
            addHallZ(builder, doorX[0], az, bz, floorY, depth);
            addHallX(builder, doorX[0], doorX[1], bz, floorY, depth);
        }
    }

    private static int axisGap(int aMin, int aMax, int bMin, int bMax) {
        if (aMax < bMin) {
            return bMin - aMax;
        }
        if (bMax < aMin) {
            return aMin - bMax;
        }
        return 0;
    }

    private static int[] facingCoordinates(int aMin, int aMax, int bMin, int bMax,
                                           int aCenter, int bCenter) {
        int aDoorMin = insetMin(aMin, aMax);
        int aDoorMax = insetMax(aMin, aMax);
        int bDoorMin = insetMin(bMin, bMax);
        int bDoorMax = insetMax(bMin, bMax);
        int overlapMin = Math.max(aDoorMin, bDoorMin);
        int overlapMax = Math.min(aDoorMax, bDoorMax);
        if (overlapMin <= overlapMax) {
            int shared = (overlapMin + overlapMax) / 2;
            return new int[]{shared, shared};
        }
        return new int[]{clamp(bCenter, aDoorMin, aDoorMax), clamp(aCenter, bDoorMin, bDoorMax)};
    }

    private static int insetMin(int min, int max) {
        return min + Math.min(3, Math.max(1, (max - min) / 2));
    }

    private static int insetMax(int min, int max) {
        return max - Math.min(3, Math.max(1, (max - min) / 2));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void addHallX(StructurePiecesBuilder builder, int x1, int x2, int z,
                                 int floorY, int depth) {
        if (x1 == x2) {
            return;
        }
        builder.addPiece(new TriangleStoneTownPassagePiece(new BoundingBox(
                Math.min(x1, x2), floorY - 1, z - 3,
                Math.max(x1, x2), floorY + 5, z + 3),
                TriangleStoneTownPassagePiece.Mode.HALL_X, depth));
    }

    private static void addHallZ(StructurePiecesBuilder builder, int x, int z1, int z2,
                                 int floorY, int depth) {
        if (z1 == z2) {
            return;
        }
        builder.addPiece(new TriangleStoneTownPassagePiece(new BoundingBox(
                x - 3, floorY - 1, Math.min(z1, z2),
                x + 3, floorY + 5, Math.max(z1, z2)),
                TriangleStoneTownPassagePiece.Mode.HALL_Z, depth));
    }

    private static void hall(StructurePiecesBuilder builder, int x1, int z1, int x2, int z2,
                             int floorY, int depth) {
        addHallX(builder, x1, x2, z1, floorY, depth);
        addHallZ(builder, x2, z1, z2, floorY, depth);
    }

    private static void stairZ(StructurePiecesBuilder builder, int x, int highZ, int lowZ,
                               int lowFloorY, int highFloorY, int depth) {
        TriangleStoneTownPassagePiece.Mode mode = lowZ > highZ
                ? TriangleStoneTownPassagePiece.Mode.STAIR_Z_POSITIVE_DOWN
                : TriangleStoneTownPassagePiece.Mode.STAIR_Z_NEGATIVE_DOWN;
        builder.addPiece(new TriangleStoneTownPassagePiece(new BoundingBox(
                x - 4, lowFloorY - 2, Math.min(highZ, lowZ),
                x + 4, highFloorY + 4, Math.max(highZ, lowZ)), mode, depth));
    }

    private static void stairX(StructurePiecesBuilder builder, int highX, int lowX, int z,
                               int lowFloorY, int highFloorY, int depth) {
        TriangleStoneTownPassagePiece.Mode mode = lowX > highX
                ? TriangleStoneTownPassagePiece.Mode.STAIR_X_POSITIVE_DOWN
                : TriangleStoneTownPassagePiece.Mode.STAIR_X_NEGATIVE_DOWN;
        builder.addPiece(new TriangleStoneTownPassagePiece(new BoundingBox(
                Math.min(highX, lowX), lowFloorY - 2, z - 4,
                Math.max(highX, lowX), highFloorY + 4, z + 4), mode, depth));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.TRIANGLE_STONE_TOWN.get();
    }

    private record WeightedRoom(String name, int weight) {
    }
}
