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
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** Code-built hollow passages guarantee that every room remains physically explorable underground. */
public final class TriangleStoneTownPassagePiece extends StructurePiece {
    public enum Mode {
        HALL,
        HALL_X,
        HALL_Z,
        STAIR_X_POSITIVE_DOWN,
        STAIR_X_NEGATIVE_DOWN,
        STAIR_Z_POSITIVE_DOWN,
        STAIR_Z_NEGATIVE_DOWN
    }

    private static final BlockState BRICKS = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    private static final BlockState CRACKED = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
    private static final BlockState TILES = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private final Mode mode;

    public TriangleStoneTownPassagePiece(BoundingBox bounds, Mode mode, int depth) {
        super(ModStructures.TRIANGLE_STONE_TOWN_PASSAGE_PIECE.get(), depth, bounds);
        this.mode = mode;
    }

    public TriangleStoneTownPassagePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.TRIANGLE_STONE_TOWN_PASSAGE_PIECE.get(), tag);
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
            int floorY = highFloor - Math.round((float) logical * drop / Math.max(1, run - 1));
            for (int side = -2; side <= 2; side++) {
                int x = alongX ? i + 1 : width / 2 + side;
                int z = alongX ? depth / 2 + side : i + 1;
                if (x <= 0 || x >= width - 1 || z <= 0 || z >= depth - 1) {
                    continue;
                }
                for (int y = 1; y < floorY; y++) {
                    placeBlock(level, BRICKS, x, y, z, chunkBounds);
                }
                placeBlock(level, side == 0 ? stair : TILES, x, floorY, z, chunkBounds);
                for (int y = floorY + 1; y <= Math.min(height - 2, floorY + 3); y++) {
                    placeBlock(level, Blocks.AIR.defaultBlockState(), x, y, z, chunkBounds);
                }
            }
        }
    }
}
