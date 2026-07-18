package com.example.examplemod.worldgen.structure;

import com.example.examplemod.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.util.RandomSource;

/** One fixed, non-random ninth of the sky arena. */
public final class SkyArenaStructurePiece extends TemplateStructurePiece {
    public SkyArenaStructurePiece(StructureTemplateManager manager, String name, BlockPos origin,
                                  Rotation rotation) {
        super(ModStructures.SKY_ARENA_TEMPLATE_PIECE.get(), 0, manager,
                SkyArenaStructure.templateId(name), name, settings(rotation), origin);
    }

    public SkyArenaStructurePiece(StructureTemplateManager manager, CompoundTag tag) {
        super(ModStructures.SKY_ARENA_TEMPLATE_PIECE.get(), tag, manager,
                ignored -> settings(Rotation.NONE));
    }

    public static boolean placeDirect(ServerLevel level, String name, BlockPos origin) {
        StructureTemplate template = level.getStructureManager().getOrCreate(SkyArenaStructure.templateId(name));
        return template.placeInWorld(level, origin, origin, settings(Rotation.NONE), level.random,
                Block.UPDATE_ALL);
    }

    private static StructurePlaceSettings settings(Rotation rotation) {
        return new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation)
                .setIgnoreEntities(false)
                .setKeepLiquids(false)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos position, ServerLevelAccessor level,
                                    RandomSource random, BoundingBox bounds) {
        // The container, seal cores and summon runes are real blocks authored in arena_center.
    }
}
