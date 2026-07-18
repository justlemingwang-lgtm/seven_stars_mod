package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.worldgen.structure.TriangleStoneTownPassagePiece;
import com.example.examplemod.worldgen.structure.TriangleStoneTownPiece;
import com.example.examplemod.worldgen.structure.TriangleStoneTownStructure;
import com.example.examplemod.worldgen.structure.SkyArenaStructure;
import com.example.examplemod.worldgen.structure.SkyArenaStructurePiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, ExampleMod.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, ExampleMod.MODID);

    public static final RegistryObject<StructureType<TriangleStoneTownStructure>> TRIANGLE_STONE_TOWN =
            STRUCTURES.register("triangle_stone_town", () -> () -> TriangleStoneTownStructure.CODEC);
    public static final RegistryObject<StructurePieceType> TRIANGLE_STONE_TOWN_TEMPLATE_PIECE =
            PIECES.register("triangle_stone_town_template", () ->
                    (StructurePieceType.StructureTemplateType) TriangleStoneTownPiece::new);
    public static final RegistryObject<StructurePieceType> TRIANGLE_STONE_TOWN_PASSAGE_PIECE =
            PIECES.register("triangle_stone_town_passage", () -> TriangleStoneTownPassagePiece::new);
    public static final RegistryObject<StructureType<SkyArenaStructure>> SKY_ARENA =
            STRUCTURES.register("sky_arena", () -> () -> SkyArenaStructure.CODEC);
    public static final RegistryObject<StructurePieceType> SKY_ARENA_TEMPLATE_PIECE =
            PIECES.register("sky_arena_template", () ->
                    (StructurePieceType.StructureTemplateType) SkyArenaStructurePiece::new);

    private ModStructures() {
    }

    public static void register(IEventBus eventBus) {
        STRUCTURES.register(eventBus);
        PIECES.register(eventBus);
    }
}
