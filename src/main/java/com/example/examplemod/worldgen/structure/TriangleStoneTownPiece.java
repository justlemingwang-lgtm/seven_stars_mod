package com.example.examplemod.worldgen.structure;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/** Places one authored room and opens only the connectors accepted by the stronghold-style generator. */
public final class TriangleStoneTownPiece extends TemplateStructurePiece {
    private static final String OPEN_DOORS_TAG = "OpenDoors";
    private final List<OpenDoor> openDoors = new ArrayList<>();

    public TriangleStoneTownPiece(StructureTemplateManager manager, String name, BlockPos minimumCorner,
                                  Rotation rotation, int depth) {
        this(manager, templateId(name), minimumCorner, rotation, depth, false);
    }

    private TriangleStoneTownPiece(StructureTemplateManager manager, ResourceLocation templateId,
                                   BlockPos position, Rotation rotation, int depth, boolean positionIsTemplateOrigin) {
        super(ModStructures.TRIANGLE_STONE_TOWN_TEMPLATE_PIECE.get(), depth, manager, templateId,
                templateId.toString(), settings(rotation), placementPosition(manager, templateId, position,
                        rotation, positionIsTemplateOrigin));
    }

    public TriangleStoneTownPiece(StructureTemplateManager manager, CompoundTag tag) {
        super(ModStructures.TRIANGLE_STONE_TOWN_TEMPLATE_PIECE.get(), tag, manager,
                ignored -> settings(readRotation(tag)));
        int[] serializedDoors = tag.getIntArray(OPEN_DOORS_TAG);
        for (int index = 0; index + 3 < serializedDoors.length; index += 4) {
            openDoors.add(new OpenDoor(new BlockPos(serializedDoors[index], serializedDoors[index + 1],
                    serializedDoors[index + 2]), Direction.from3DDataValue(serializedDoors[index + 3])));
        }
    }

    static TriangleStoneTownPiece atTemplateOrigin(StructureTemplateManager manager, String name,
                                                   BlockPos templateOrigin, Rotation rotation, int depth) {
        return new TriangleStoneTownPiece(manager, templateId(name), templateOrigin, rotation, depth, true);
    }

    static ResourceLocation templateId(String name) {
        return ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "triangle_stone_town/" + name);
    }

    private static BlockPos placementPosition(StructureTemplateManager manager, ResourceLocation templateId,
                                              BlockPos position, Rotation rotation,
                                              boolean positionIsTemplateOrigin) {
        return positionIsTemplateOrigin ? position : manager.getOrCreate(templateId)
                .getZeroPositionWithTransform(position, Mirror.NONE, rotation);
    }

    private static Rotation readRotation(CompoundTag tag) {
        try {
            return Rotation.valueOf(tag.getString("Rot"));
        } catch (IllegalArgumentException ignored) {
            return Rotation.NONE;
        }
    }

    private static StructurePlaceSettings settings(Rotation rotation) {
        return new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation)
                .setIgnoreEntities(false)
                .setKeepLiquids(false)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
                .addProcessor(JigsawReplacementProcessor.INSTANCE);
    }

    public String assetName() {
        int slash = templateName.lastIndexOf('/');
        return slash >= 0 ? templateName.substring(slash + 1) : templateName;
    }

    List<StructureTemplate.StructureBlockInfo> jigsawBlocks() {
        return template.filterBlocks(templatePosition,
                new StructurePlaceSettings().setRotation(placeSettings.getRotation()), Blocks.JIGSAW, true);
    }

    void openDoor(BlockPos connectorPosition, Direction facing) {
        OpenDoor door = new OpenDoor(connectorPosition.immutable(), facing);
        if (!openDoors.contains(door)) {
            openDoors.add(door);
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext context,
                                         CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString("Rot", placeSettings.getRotation().name());
        int[] serializedDoors = new int[openDoors.size() * 4];
        for (int index = 0; index < openDoors.size(); index++) {
            OpenDoor door = openDoors.get(index);
            int offset = index * 4;
            serializedDoors[offset] = door.position().getX();
            serializedDoors[offset + 1] = door.position().getY();
            serializedDoors[offset + 2] = door.position().getZ();
            serializedDoors[offset + 3] = door.facing().get3DDataValue();
        }
        tag.putIntArray(OPEN_DOORS_TAG, serializedDoors);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBounds, ChunkPos chunkPos, BlockPos pivot) {
        super.postProcess(level, structureManager, chunkGenerator, random, chunkBounds, chunkPos, pivot);
        for (OpenDoor door : openDoors) {
            carveDoor(level, chunkBounds, door);
        }
    }

    /** Matches the vanilla stronghold's three-wide, three-high small doorway. */
    private static void carveDoor(WorldGenLevel level, BoundingBox chunkBounds, OpenDoor door) {
        boolean wallRunsAlongX = door.facing().getAxis() == Direction.Axis.Z;
        for (int across = -1; across <= 1; across++) {
            for (int up = 0; up < 3; up++) {
                BlockPos position = wallRunsAlongX
                        ? door.position().offset(across, up, 0)
                        : door.position().offset(0, up, across);
                if (chunkBounds.isInside(position)) {
                    level.setBlock(position, Blocks.CAVE_AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos position, ServerLevelAccessor level,
                                    RandomSource random, BoundingBox bounds) {
        // Loot and spawners are authored directly in the NBT templates.
    }

    private record OpenDoor(BlockPos position, Direction facing) {
    }
}
