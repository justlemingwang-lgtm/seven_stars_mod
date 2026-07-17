package com.example.examplemod.worldgen.structure;

import com.example.examplemod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.ArrayList;
import java.util.List;

/** Code-built hollow passages guarantee that every room remains physically explorable underground. */
public final class TriangleStoneTownPassagePiece extends StructurePiece {
    public enum Mode {
        HALL,
        HALL_X,
        HALL_Z,
        STAIR_X_POSITIVE_DOWN,
        STAIR_X_NEGATIVE_DOWN,
        STAIR_Z_POSITIVE_DOWN,
        STAIR_Z_NEGATIVE_DOWN,
        ENTRANCE_SPIRAL
    }

    private static final BlockState BRICKS = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    private static final BlockState CRACKED = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
    private static final BlockState TILES = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private final Mode mode;

    public TriangleStoneTownPassagePiece(BoundingBox bounds, Mode mode, int depth) {
        super(ModStructures.TRIANGLE_STONE_TOWN_PASSAGE_PIECE.get(), depth, bounds);
        // StructurePiece treats coordinates as absolute when orientation is null.
        // SOUTH provides the standard min-corner local coordinate transform.
        setOrientation(Direction.SOUTH);
        this.mode = mode;
    }

    public TriangleStoneTownPassagePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.TRIANGLE_STONE_TOWN_PASSAGE_PIECE.get(), tag);
        setOrientation(Direction.SOUTH);
        Mode parsed;
        try {
            parsed = Mode.valueOf(tag.getString("Mode"));
        } catch (IllegalArgumentException ignored) {
            parsed = Mode.HALL;
        }
        this.mode = parsed;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Mode", mode.name());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBounds, ChunkPos chunkPos, BlockPos pivot) {
        int width = boundingBox.getXSpan();
        int height = boundingBox.getYSpan();
        int depth = boundingBox.getZSpan();

        if (mode == Mode.ENTRANCE_SPIRAL) {
            buildEntranceSpiral(level, chunkBounds, width, height, depth);
            return;
        }

        generateBox(level, chunkBounds, 0, 0, 0, width - 1, height - 1, depth - 1,
                BRICKS, BRICKS, false);
        if (width > 2 && height > 2 && depth > 2) {
            generateBox(level, chunkBounds, 1, 1, 1, width - 2, height - 2, depth - 2,
                    Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
        }

        if (isHall()) {
            decorateHall(level, chunkBounds, random, width, height, depth);
            openHallEnds(level, chunkBounds, width, height, depth);
        } else {
            buildStair(level, chunkBounds, width, height, depth);
            openStairEnds(level, chunkBounds, width, height, depth);
        }
    }

    private void openHallEnds(WorldGenLevel level, BoundingBox chunkBounds,
                              int width, int height, int depth) {
        boolean alongX = mode == Mode.HALL_X || (mode == Mode.HALL && width >= depth);
        if (alongX) {
            for (int z = 1; z < depth - 1; z++) {
                for (int y = 1; y < height - 1; y++) {
                    placeBlock(level, Blocks.AIR.defaultBlockState(), 0, y, z, chunkBounds);
                    placeBlock(level, Blocks.AIR.defaultBlockState(), width - 1, y, z, chunkBounds);
                }
            }
        } else {
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    placeBlock(level, Blocks.AIR.defaultBlockState(), x, y, 0, chunkBounds);
                    placeBlock(level, Blocks.AIR.defaultBlockState(), x, y, depth - 1, chunkBounds);
                }
            }
        }
    }

    private boolean isHall() {
        return mode == Mode.HALL || mode == Mode.HALL_X || mode == Mode.HALL_Z;
    }

    private void openStairEnds(WorldGenLevel level, BoundingBox chunkBounds,
                               int width, int height, int depth) {
        boolean alongX = mode == Mode.STAIR_X_POSITIVE_DOWN || mode == Mode.STAIR_X_NEGATIVE_DOWN;
        boolean positiveDown = mode == Mode.STAIR_X_POSITIVE_DOWN || mode == Mode.STAIR_Z_POSITIVE_DOWN;
        int highFloor = height - 5;
        int lowFloor = 2;
        int minimumEndFloor = positiveDown ? highFloor : lowFloor;
        int maximumEndFloor = positiveDown ? lowFloor : highFloor;

        if (alongX) {
            for (int z = 1; z < depth - 1; z++) {
                clearHeadroom(level, chunkBounds, 0, minimumEndFloor, z, height);
                clearHeadroom(level, chunkBounds, width - 1, maximumEndFloor, z, height);
            }
        } else {
            for (int x = 1; x < width - 1; x++) {
                clearHeadroom(level, chunkBounds, x, minimumEndFloor, 0, height);
                clearHeadroom(level, chunkBounds, x, maximumEndFloor, depth - 1, height);
            }
        }
    }

    private void clearHeadroom(WorldGenLevel level, BoundingBox chunkBounds,
                               int x, int floorY, int z, int height) {
        for (int y = floorY + 1; y <= Math.min(height - 2, floorY + 3); y++) {
            placeBlock(level, Blocks.AIR.defaultBlockState(), x, y, z, chunkBounds);
        }
    }

    private void decorateHall(WorldGenLevel level, BoundingBox chunkBounds, RandomSource random,
                              int width, int height, int depth) {
        for (int x = 1; x < width - 1; x++) {
            for (int z = 1; z < depth - 1; z++) {
                placeBlock(level, random.nextFloat() < 0.14F ? CRACKED : TILES, x, 0, z, chunkBounds);
            }
        }
        int run = Math.max(width, depth);
        for (int i = 7; i < run - 2; i += 12) {
            int x = width >= depth ? i : Math.max(1, width / 2);
            int z = depth > width ? i : Math.max(1, depth / 2);
            placeBlock(level, Blocks.SOUL_LANTERN.defaultBlockState(), x, 1, z, chunkBounds);
        }
    }

    private void buildStair(WorldGenLevel level, BoundingBox chunkBounds, int width, int height, int depth) {
        boolean alongX = mode == Mode.STAIR_X_POSITIVE_DOWN || mode == Mode.STAIR_X_NEGATIVE_DOWN;
        boolean positiveDown = mode == Mode.STAIR_X_POSITIVE_DOWN || mode == Mode.STAIR_Z_POSITIVE_DOWN;
        int run = (alongX ? width : depth) - 2;
        int highFloor = height - 5;
        int drop = Math.max(1, highFloor - 2);
        Direction ascentDirection;
        if (alongX) {
            ascentDirection = positiveDown ? Direction.WEST : Direction.EAST;
        } else {
            ascentDirection = positiveDown ? Direction.NORTH : Direction.SOUTH;
        }
        BlockState stair = Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, ascentDirection);

        for (int i = 0; i < run; i++) {
            int logical = positiveDown ? i : run - 1 - i;
            int surfaceHalf = highFloor * 2
                    - Math.round((float) logical * drop * 2 / Math.max(1, run - 1));
            int nextLogical = Math.max(0, logical - 1);
            int nextSurfaceHalf = highFloor * 2
                    - Math.round((float) nextLogical * drop * 2 / Math.max(1, run - 1));
            boolean fullBlockRise = nextSurfaceHalf - surfaceHalf >= 2;
            int floorY = fullBlockRise ? surfaceHalf / 2 : Math.max(0, (surfaceHalf - 1) / 2);
            BlockState surface = fullBlockRise
                    ? stair
                    : Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState().setValue(
                            SlabBlock.TYPE, surfaceHalf % 2 == 0 ? SlabType.TOP : SlabType.BOTTOM);
            for (int side = -2; side <= 2; side++) {
                int x = alongX ? i + 1 : width / 2 + side;
                int z = alongX ? depth / 2 + side : i + 1;
                if (x <= 0 || x >= width - 1 || z <= 0 || z >= depth - 1) {
                    continue;
                }
                for (int y = 1; y < floorY; y++) {
                    placeBlock(level, BRICKS, x, y, z, chunkBounds);
                }
                placeBlock(level, surface, x, floorY, z, chunkBounds);
                for (int y = floorY + 1; y <= Math.min(height - 2, floorY + 3); y++) {
                    placeBlock(level, Blocks.AIR.defaultBlockState(), x, y, z, chunkBounds);
                }
            }
        }
    }

    /**
     * Variable-height entrance flight. Its top always meets the surface landmark,
     * while its north landing is fixed to the underground town's entry level.
     */
    private void buildEntranceSpiral(WorldGenLevel level, BoundingBox chunkBounds,
                                     int width, int height, int depth) {
        // Use an explicit min-corner transform for this variable-height piece.
        // This keeps it independent of StructurePiece's orientation rules and
        // guarantees that every block lands inside this piece's world bounds.
        fillEntranceBox(level, chunkBounds, BRICKS,
                0, 0, 0, width - 1, height - 1, depth - 1);
        fillEntranceBox(level, chunkBounds, Blocks.AIR.defaultBlockState(),
                1, 1, 1, width - 2, height - 2, depth - 2);

        int centerX = width / 2;
        int centerZ = depth / 2;
        int highStanding = height - 4;
        int lowStanding = 1;

        // A solid core makes the repeated square flights read as one spiral tower.
        fillEntranceBox(level, chunkBounds, BRICKS, centerX - 1, 1, centerZ - 1,
                centerX + 1, height - 2, centerZ + 1);

        // Landings are placed first so the route can retain a transition stair on
        // either end when the height distribution calls for one.
        fillEntranceBox(level, chunkBounds, TILES, centerX - 2, highStanding - 1, centerZ + 4,
                centerX + 2, highStanding - 1, depth - 1);
        fillEntranceBox(level, chunkBounds, TILES, centerX - 2, 0, 0,
                centerX + 2, 0, centerZ - 4);

        List<BlockPos> ring = squareRing(centerX, centerZ);
        List<BlockPos> finalArc = northArc(centerX, centerZ);
        int drop = highStanding - lowStanding;
        int ringTransitions = ring.size() - 1;
        int finalTransitions = finalArc.size() - 1;
        int requiredTransitions = drop * 3;
        int fullLoops = Math.max(0,
                (Math.max(0, requiredTransitions - finalTransitions) + ringTransitions - 1)
                        / ringTransitions);

        List<BlockPos> route = new ArrayList<>(1 + fullLoops * ringTransitions + finalTransitions);
        route.add(ring.get(0));
        for (int loop = 0; loop < fullLoops; loop++) {
            route.addAll(ring.subList(1, ring.size()));
        }
        route.addAll(finalArc.subList(1, finalArc.size()));

        int transitions = route.size() - 1;
        int previousStanding = highStanding;
        for (int index = 0; index < route.size(); index++) {
            BlockPos current = route.get(index);
            int standing = highStanding - index * drop / Math.max(1, transitions);
            BlockPos previous = route.get(Math.max(0, index - 1));
            Direction ascent = horizontalDirection(
                    previous.getX() - current.getX(), previous.getZ() - current.getZ());

            BlockPos neighbor = route.get(index < route.size() - 1 ? index + 1 : index - 1);
            int travelX = neighbor.getX() - current.getX();
            int outerX;
            int outerZ;
            if (travelX != 0) {
                outerX = 0;
                outerZ = current.getZ() > centerZ ? 1 : -1;
            } else {
                outerX = current.getX() > centerX ? 1 : -1;
                outerZ = 0;
            }

            placeSpiralTread(level, chunkBounds, current.getX(), current.getZ(),
                    standing, previousStanding, ascent);
            placeSpiralTread(level, chunkBounds, current.getX() + outerX, current.getZ() + outerZ,
                    standing, previousStanding, ascent);
            previousStanding = standing;
        }

        // Three-wide doors line up exactly with the entrance template to the
        // south and the town-square template to the north.
        for (int across = -1; across <= 1; across++) {
            for (int up = 0; up < 3; up++) {
                placeEntranceBlock(level, chunkBounds, Blocks.AIR.defaultBlockState(),
                        centerX + across, highStanding + up, depth - 1);
                placeEntranceBlock(level, chunkBounds, Blocks.AIR.defaultBlockState(),
                        centerX + across, lowStanding + up, 0);
            }
        }
    }

    private static List<BlockPos> squareRing(int centerX, int centerZ) {
        List<BlockPos> route = new ArrayList<>();
        for (int x = centerX; x >= centerX - 4; x--) {
            route.add(new BlockPos(x, 0, centerZ + 4));
        }
        for (int z = centerZ + 3; z >= centerZ - 4; z--) {
            route.add(new BlockPos(centerX - 4, 0, z));
        }
        for (int x = centerX - 3; x <= centerX + 4; x++) {
            route.add(new BlockPos(x, 0, centerZ - 4));
        }
        for (int z = centerZ - 3; z <= centerZ + 4; z++) {
            route.add(new BlockPos(centerX + 4, 0, z));
        }
        for (int x = centerX + 3; x >= centerX; x--) {
            route.add(new BlockPos(x, 0, centerZ + 4));
        }
        return route;
    }

    private static List<BlockPos> northArc(int centerX, int centerZ) {
        List<BlockPos> route = new ArrayList<>();
        for (int x = centerX; x >= centerX - 4; x--) {
            route.add(new BlockPos(x, 0, centerZ + 4));
        }
        for (int z = centerZ + 3; z >= centerZ - 4; z--) {
            route.add(new BlockPos(centerX - 4, 0, z));
        }
        for (int x = centerX - 3; x <= centerX; x++) {
            route.add(new BlockPos(x, 0, centerZ - 4));
        }
        return route;
    }

    private void placeSpiralTread(WorldGenLevel level, BoundingBox chunkBounds,
                                  int x, int z, int standing, int previousStanding,
                                  Direction ascent) {
        boolean descendsHere = previousStanding > standing;
        int blockY = descendsHere ? standing : standing - 1;
        BlockState tread = descendsHere
                ? Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, ascent)
                : Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
        if (blockY > 0) {
            placeEntranceBlock(level, chunkBounds, BRICKS, x, blockY - 1, z);
        }
        placeEntranceBlock(level, chunkBounds, tread, x, blockY, z);
        for (int y = blockY + 1; y <= Math.min(boundingBox.getYSpan() - 2, blockY + 3); y++) {
            placeEntranceBlock(level, chunkBounds, Blocks.AIR.defaultBlockState(), x, y, z);
        }
    }

    private void fillEntranceBox(WorldGenLevel level, BoundingBox chunkBounds, BlockState state,
                                 int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    placeEntranceBlock(level, chunkBounds, state, x, y, z);
                }
            }
        }
    }

    private void placeEntranceBlock(WorldGenLevel level, BoundingBox chunkBounds, BlockState state,
                                    int localX, int localY, int localZ) {
        BlockPos worldPosition = new BlockPos(
                boundingBox.minX() + localX,
                boundingBox.minY() + localY,
                boundingBox.minZ() + localZ);
        if (chunkBounds.isInside(worldPosition)) {
            level.setBlock(worldPosition, state, 2);
        }
    }

    private static Direction horizontalDirection(int deltaX, int deltaZ) {
        if (deltaX > 0) {
            return Direction.EAST;
        }
        if (deltaX < 0) {
            return Direction.WEST;
        }
        return deltaZ > 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
