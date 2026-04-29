package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.PlaceholderStructureBlock;
import com.example.examplemod.block.GoatHornAltarBlock;
import com.example.examplemod.block.SoulCalmingLampBlock;
import com.example.examplemod.block.WritingTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ExampleMod.MODID);

    public static final RegistryObject<Block> WRITING_TABLE = BLOCKS.register("writing_table",
            () -> new WritingTableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F)));
    public static final RegistryObject<Block> QI_CRYSTAL_ORE = BLOCKS.register("qi_crystal_ore",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 3.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> DEEPSLATE_QI_CRYSTAL_ORE = BLOCKS.register("deepslate_qi_crystal_ore",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> QI_CRYSTAL_BLOCK = BLOCKS.register("qi_crystal_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(3.0F, 6.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> QI_CRYSTAL_LAMP = BLOCKS.register("qi_crystal_lamp",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(1.5F, 3.0F).lightLevel(state -> 14).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> QI_CRYSTAL_BRICKS = BLOCKS.register("qi_crystal_bricks",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(3.0F, 6.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> STAR_HOOF_CHARM_BLOCK = BLOCKS.register("star_hoof_charm_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.0F, 2.0F).lightLevel(state -> 4)));
    public static final RegistryObject<Block> FROST_MARROW_CANDLE = BLOCKS.register("frost_marrow_candle",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.ICE).strength(0.8F).lightLevel(state -> 10)));
    public static final RegistryObject<Block> CLAW_TROPHY_RACK = BLOCKS.register("claw_trophy_rack",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F, 2.0F)));
    public static final RegistryObject<Block> STAR_CURSED_BRICKS = BLOCKS.register("star_cursed_bricks",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(35.0F, 1200.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> CRACKED_STAR_CURSED_BRICKS = BLOCKS.register("cracked_star_cursed_bricks",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(28.0F, 900.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> STAR_RUNE_TILES = BLOCKS.register("star_rune_tiles",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(6.0F, 12.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> GOAT_HORN_ALTAR = BLOCKS.register("goat_horn_altar",
            () -> new GoatHornAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(50.0F, 1200.0F).lightLevel(state -> 4).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> SOUL_CALMING_LAMP = BLOCKS.register("soul_calming_lamp",
            () -> new SoulCalmingLampBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0F, 12.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> TRIANGLE_STONE_BRICKS = BLOCKS.register("triangle_stone_bricks",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.5F, 9.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> TRIANGLE_RUNE_TILES = BLOCKS.register("triangle_rune_tiles",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.5F, 9.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> CHOPPING_STUMP = BLOCKS.register("chopping_stump",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F)));
    public static final RegistryObject<Block> CLEAVER_RACK = BLOCKS.register("cleaver_rack",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F, 4.0F)));
    public static final RegistryObject<Block> ABANDONED_STABLE = BLOCKS.register("abandoned_stable",
            () -> new PlaceholderStructureBlock(PlaceholderStructureBlock.Kind.STABLE, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F)));
    public static final RegistryObject<Block> FROZEN_OBSERVATORY = BLOCKS.register("frozen_observatory",
            () -> new PlaceholderStructureBlock(PlaceholderStructureBlock.Kind.OBSERVATORY, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F)));
    public static final RegistryObject<Block> ABANDONED_KENNEL = BLOCKS.register("abandoned_kennel",
            () -> new PlaceholderStructureBlock(PlaceholderStructureBlock.Kind.KENNEL, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
