package com.example.examplemod.worldgen.structure;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.registry.ModStructures;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.Optional;

/** Deterministically assembles the 113 x 113 sky arena from nine editable templates. */
public final class SkyArenaStructure extends Structure {
    public static final Codec<SkyArenaStructure> CODEC = simpleCodec(SkyArenaStructure::new);
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "sky_arena");
    public static final TagKey<Structure> LOCATABLE_TAG = TagKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "locatable_by_azure_dragon_eye"));

    public static final int ARENA_RADIUS = 56;
    public static final int DIAMETER = 113;
    public static final int FLOOR_LEVEL_IN_TEMPLATE = 8;
    public static final int TEMPLATE_HEIGHT = 21;
    public static final int WALL_HEIGHT = 8;
    public static final int MIN_FLOOR_Y = 208;
    public static final int MAX_FLOOR_Y = 224;

    private static final List<TemplatePlacement> TEMPLATES = List.of(
            new TemplatePlacement("arena_north_west", -56, -56, 38, 38),
            new TemplatePlacement("arena_north", -18, -56, 37, 38),
            new TemplatePlacement("arena_north_east", 19, -56, 38, 38),
            new TemplatePlacement("arena_west", -56, -18, 38, 37),
            new TemplatePlacement("arena_center", -18, -18, 37, 37),
            new TemplatePlacement("arena_east", 19, -18, 38, 37),
            new TemplatePlacement("arena_south_west", -56, 19, 38, 38),
            new TemplatePlacement("arena_south", -18, 19, 37, 38),
            new TemplatePlacement("arena_south_east", 19, 19, 38, 38)
    );

    public SkyArenaStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int floorY = MIN_FLOOR_Y + context.random().nextInt(MAX_FLOOR_Y - MIN_FLOOR_Y + 1);
        int originY = floorY - FLOOR_LEVEL_IN_TEMPLATE;
        if (originY < context.heightAccessor().getMinBuildHeight()
                || originY + TEMPLATE_HEIGHT > context.heightAccessor().getMaxBuildHeight()) {
            return Optional.empty();
        }
        BlockPos center = new BlockPos(context.chunkPos().getMiddleBlockX(), floorY,
                context.chunkPos().getMiddleBlockZ());
        return Optional.of(new GenerationStub(center, builder -> addPieces(builder,
                context.structureTemplateManager(), center)));
    }

    public static void addPieces(StructurePiecesBuilder builder, StructureTemplateManager templates,
                                 BlockPos centerAtFloor) {
        int originY = centerAtFloor.getY() - FLOOR_LEVEL_IN_TEMPLATE;
        for (TemplatePlacement placement : TEMPLATES) {
            BlockPos origin = new BlockPos(centerAtFloor.getX() + placement.offsetX(), originY,
                    centerAtFloor.getZ() + placement.offsetZ());
            builder.addPiece(new SkyArenaStructurePiece(templates, placement.name(), origin, Rotation.NONE));
        }
    }

    public static List<TemplatePlacement> templates() {
        return TEMPLATES;
    }

    public static ResourceLocation templateId(String name) {
        return ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "sky_arena/" + name);
    }

    public static int highestWorldY(int floorY) {
        return floorY - FLOOR_LEVEL_IN_TEMPLATE + TEMPLATE_HEIGHT - 1;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SKY_ARENA.get();
    }

    public record TemplatePlacement(String name, int offsetX, int offsetZ, int sizeX, int sizeZ) {
    }
}
