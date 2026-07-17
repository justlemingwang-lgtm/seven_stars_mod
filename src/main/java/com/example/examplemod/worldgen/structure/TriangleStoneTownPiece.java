package com.example.examplemod.worldgen.structure;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.util.RandomSource;

/** Places one authored room. Connectivity is owned by the parent structure, not by jigsaw depth. */
public final class TriangleStoneTownPiece extends TemplateStructurePiece {
    public TriangleStoneTownPiece(StructureTemplateManager manager, String name, BlockPos minimumCorner,
                                  Rotation rotation, int depth) {
        this(manager, templateId(name), minimumCorner, rotation, depth);
    }

    private TriangleStoneTownPiece(StructureTemplateManager manager, ResourceLocation templateId,
                                   BlockPos minimumCorner, Rotation rotation, int depth) {
        super(ModStructures.TRIANGLE_STONE_TOWN_TEMPLATE_PIECE.get(), depth, manager, templateId,
                templateId.toString(), settings(rotation), manager.getOrCreate(templateId)
                        .getZeroPositionWithTransform(minimumCorner, Mirror.NONE, rotation));
    }

    public TriangleStoneTownPiece(StructureTemplateManager manager, CompoundTag tag) {
        super(ModStructures.TRIANGLE_STONE_TOWN_TEMPLATE_PIECE.get(), tag, manager,
                ignored -> settings(readRotation(tag)));
    }

    private static ResourceLocation templateId(String name) {
        return new ResourceLocation(ExampleMod.MODID, "triangle_stone_town/" + name);
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

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext context,
                                         CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString("Rot", placeSettings.getRotation().name());
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos position, ServerLevelAccessor level,
                                    RandomSource random, BoundingBox bounds) {
        // Loot and spawners are authored directly in the NBT templates.
    }
}
