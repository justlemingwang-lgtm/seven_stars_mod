package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.item.AbsorberItem;
import com.example.examplemod.item.AzureDragonScaleItem;
import com.example.examplemod.item.AzureDragonEyeItem;
import com.example.examplemod.item.BloodyCleaverItem;
import com.example.examplemod.item.ConsumerItem;
import com.example.examplemod.item.CooldownChestplateItem;
import com.example.examplemod.item.CrackedClawDaggerItem;
import com.example.examplemod.item.FrostMarrowWandItem;
import com.example.examplemod.item.GoatHornArmorItem;
import com.example.examplemod.item.QiFoodItem;
import com.example.examplemod.item.QiHelmetItem;
import com.example.examplemod.item.SevenStarScrollItem;
import com.example.examplemod.item.SevenStarsCodexItem;
import com.example.examplemod.item.StarHoofSpearItem;
import com.example.examplemod.item.StarReinBellItem;
import com.example.examplemod.item.SpellScrollItem;
import com.example.examplemod.item.SpellFragmentItem;
import com.example.examplemod.item.TriangleArmorItem;
import com.example.examplemod.skill.SkillRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.food.FoodProperties;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ExampleMod.MODID);

    public static final RegistryObject<Item> ABSORBER = ITEMS.register("absorber",
            () -> new AbsorberItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CONSUMER = ITEMS.register("consumer",
            () -> new ConsumerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPELL_FRAGMENT = ITEMS.register("spell_fragment",
            () -> new SpellFragmentItem(new Item.Properties()));
    public static final RegistryObject<Item> SEVEN_STAR_SCROLL = ITEMS.register("seven_star_scroll",
            () -> new SevenStarScrollItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SEVEN_STARS_CODEX = ITEMS.register("seven_stars_codex",
            () -> new SevenStarsCodexItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AZURE_DRAGON_SCALE = ITEMS.register("azure_dragon_scale",
            () -> new AzureDragonScaleItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> AZURE_DRAGON_EYE = ITEMS.register("azure_dragon_eye",
            () -> new AzureDragonEyeItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> LOST_STAR_MAGIC_TOKEN = ITEMS.register("lost_star_magic_token",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> QI_HELMET = ITEMS.register("qi_helmet",
            () -> new QiHelmetItem(new Item.Properties()));
    public static final RegistryObject<Item> COOLDOWN_CHESTPLATE = ITEMS.register("cooldown_chestplate",
            () -> new CooldownChestplateItem(new Item.Properties()));
    public static final RegistryObject<Item> GOAT_HORN_ARMOR = ITEMS.register("goat_horn_armor",
            () -> new GoatHornArmorItem(new Item.Properties().durability(220)));
    public static final RegistryObject<Item> TRIANGLE_ARMOR = ITEMS.register("triangle_armor",
            () -> new TriangleArmorItem(new Item.Properties().durability(260)));
    public static final RegistryObject<Item> QI_CRYSTAL_SHARD = ITEMS.register("qi_crystal_shard",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> QI_CRYSTAL_CANDY = ITEMS.register("qi_crystal_candy",
            () -> new QiFoodItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(2).saturationMod(0.4F).build()), 10, false));
    public static final RegistryObject<Item> STAR_MANE_APPLE = ITEMS.register("star_mane_apple",
            () -> new QiFoodItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(4).saturationMod(0.6F).build()), 20, true));
    public static final RegistryObject<Item> FROST_CRYSTAL_CAKE_SLICE = ITEMS.register("frost_crystal_cake_slice",
            () -> new QiFoodItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(5).saturationMod(0.7F).build()), 15, false));
    public static final RegistryObject<Item> HOUND_JERKY = ITEMS.register("hound_jerky",
            () -> new QiFoodItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(6).saturationMod(0.8F).build()), 10, false));
    public static final RegistryObject<Item> COMPLETE_GOAT_HORN = ITEMS.register("complete_goat_horn", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOAT_HORN_CORE = ITEMS.register("goat_horn_core", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANCIENT_CLOTH = ITEMS.register("ancient_cloth", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STAR_RUNE_SHARD = ITEMS.register("star_rune_shard", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANCIENT_GOAT_PAGE = ITEMS.register("ancient_goat_page", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TRIANGLE_STONE_CIRCLE_PAGE = ITEMS.register("triangle_stone_circle_page", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HUNTER_PAGE = ITEMS.register("hunter_page", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOAT_HORN_SPIKE_SCROLL_FRAGMENT = ITEMS.register("goat_horn_spike_scroll_fragment", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLOODY_CLEAVER = ITEMS.register("bloody_cleaver", () -> new BloodyCleaverItem(Tiers.IRON, 3, -2.8F, new Item.Properties().durability(180)));
    public static final RegistryObject<Item> SHIELDBREAKER_BONE = ITEMS.register("shieldbreaker_bone", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DULL_BLADE_FRAGMENT = ITEMS.register("dull_blade_fragment", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHOP_SCROLL = ITEMS.register("chop_scroll",
            () -> new SpellScrollItem(SkillRegistry.CHOP, new Item.Properties()));
    public static final RegistryObject<Item> TRIANGLE_FRAGMENT = ITEMS.register("triangle_fragment", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TRIANGLE_CORE = ITEMS.register("triangle_core", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TRIANGLE_SCROLL = ITEMS.register("triangle_scroll",
            () -> new SpellScrollItem(SkillRegistry.SEVEN_SCATTERED_STRIKES, new Item.Properties()));
    public static final RegistryObject<Item> SOUL_LAMP_WICK = ITEMS.register("soul_lamp_wick", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOAT_HORN_SCROLL = ITEMS.register("goat_horn_scroll",
            () -> new SpellScrollItem(SkillRegistry.GOAT_HORN, new Item.Properties()));
    public static final RegistryObject<Item> PEGASUS_MANE = ITEMS.register("pegasus_mane", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STAR_HOOF_FRAGMENT = ITEMS.register("star_hoof_fragment", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COMPLETE_STAR_HOOF = ITEMS.register("complete_star_hoof", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FROST_SHELL_FRAGMENT = ITEMS.register("frost_shell_fragment", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FROST_POWDER = ITEMS.register("frost_powder", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FROST_MARROW_CRYSTAL = ITEMS.register("frost_marrow_crystal", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLACK_MANE = ITEMS.register("black_mane", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CRACKED_CLAW_BONE = ITEMS.register("cracked_claw_bone", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HOUND_FANG = ITEMS.register("hound_fang", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STAR_REIN_BELL = ITEMS.register("star_rein_bell", () -> new StarReinBellItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ICE_DIPPER_GAUGE = ITEMS.register("ice_dipper_gauge", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> QI_HUNTER_COLLAR = ITEMS.register("qi_hunter_collar", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STAR_HOOF_SPEAR = ITEMS.register("star_hoof_spear", () -> new StarHoofSpearItem(Tiers.IRON, 2, -2.0F, new Item.Properties().durability(350)));
    public static final RegistryObject<Item> FROST_MARROW_WAND = ITEMS.register("frost_marrow_wand", () -> new FrostMarrowWandItem(Tiers.IRON, 0, -2.2F, new Item.Properties().durability(280)));
    public static final RegistryObject<Item> CRACKED_CLAW_DAGGER = ITEMS.register("cracked_claw_dagger", () -> new CrackedClawDaggerItem(Tiers.IRON, 1, -1.6F, new Item.Properties().durability(300)));
    public static final RegistryObject<Item> LIGHT_WAVE_SCROLL = ITEMS.register("light_wave_scroll",
            () -> new SpellScrollItem(SkillRegistry.LIGHT_WAVE, new Item.Properties()));
    public static final RegistryObject<Item> SEVEN_STAR_SKILL_SCROLL = ITEMS.register("seven_star_skill_scroll",
            () -> new SpellScrollItem(SkillRegistry.SEVEN_STAR, new Item.Properties()));
    public static final RegistryObject<Item> SIX_MERIDIAN_SWORD_SCROLL = ITEMS.register("six_meridian_sword_scroll",
            () -> new SpellScrollItem(SkillRegistry.SIX_MERIDIAN_SWORD, new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_FINGER_SCROLL = ITEMS.register("golden_finger_scroll",
            () -> new SpellScrollItem(SkillRegistry.GOLDEN_FINGER, new Item.Properties()));
    public static final RegistryObject<Item> PEGASUS_STEP_SCROLL = ITEMS.register("pegasus_step_scroll",
            () -> new SpellScrollItem(SkillRegistry.PEGASUS_STEP, new Item.Properties()));
    public static final RegistryObject<Item> ICE_DIPPER_SHOT_SCROLL = ITEMS.register("ice_dipper_shot_scroll",
            () -> new SpellScrollItem(SkillRegistry.ICE_DIPPER_SHOT, new Item.Properties()));
    public static final RegistryObject<Item> HOUND_CLAW_SCROLL = ITEMS.register("hound_claw_scroll",
            () -> new SpellScrollItem(SkillRegistry.HOUND_CLAW, new Item.Properties()));
    public static final RegistryObject<Item> WRITING_TABLE = ITEMS.register("writing_table",
            () -> new BlockItem(ModBlocks.WRITING_TABLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> QI_CRYSTAL_ORE = ITEMS.register("qi_crystal_ore",
            () -> new BlockItem(ModBlocks.QI_CRYSTAL_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> DEEPSLATE_QI_CRYSTAL_ORE = ITEMS.register("deepslate_qi_crystal_ore",
            () -> new BlockItem(ModBlocks.DEEPSLATE_QI_CRYSTAL_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> QI_CRYSTAL_BLOCK = ITEMS.register("qi_crystal_block",
            () -> new BlockItem(ModBlocks.QI_CRYSTAL_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> QI_CRYSTAL_LAMP = ITEMS.register("qi_crystal_lamp",
            () -> new BlockItem(ModBlocks.QI_CRYSTAL_LAMP.get(), new Item.Properties()));
    public static final RegistryObject<Item> QI_CRYSTAL_BRICKS = ITEMS.register("qi_crystal_bricks",
            () -> new BlockItem(ModBlocks.QI_CRYSTAL_BRICKS.get(), new Item.Properties()));
    public static final RegistryObject<Item> STAR_HOOF_CHARM_BLOCK = ITEMS.register("star_hoof_charm_block",
            () -> new BlockItem(ModBlocks.STAR_HOOF_CHARM_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> FROST_MARROW_CANDLE = ITEMS.register("frost_marrow_candle",
            () -> new BlockItem(ModBlocks.FROST_MARROW_CANDLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> CLAW_TROPHY_RACK = ITEMS.register("claw_trophy_rack",
            () -> new BlockItem(ModBlocks.CLAW_TROPHY_RACK.get(), new Item.Properties()));
    public static final RegistryObject<Item> STAR_CURSED_BRICKS = ITEMS.register("star_cursed_bricks",
            () -> new BlockItem(ModBlocks.STAR_CURSED_BRICKS.get(), new Item.Properties()));
    public static final RegistryObject<Item> CRACKED_STAR_CURSED_BRICKS = ITEMS.register("cracked_star_cursed_bricks",
            () -> new BlockItem(ModBlocks.CRACKED_STAR_CURSED_BRICKS.get(), new Item.Properties()));
    public static final RegistryObject<Item> STAR_RUNE_TILES = ITEMS.register("star_rune_tiles",
            () -> new BlockItem(ModBlocks.STAR_RUNE_TILES.get(), new Item.Properties()));
    public static final RegistryObject<Item> GOAT_HORN_ALTAR = ITEMS.register("goat_horn_altar",
            () -> new BlockItem(ModBlocks.GOAT_HORN_ALTAR.get(), new Item.Properties()));
    public static final RegistryObject<Item> GOAT_HORN_PEDESTAL = ITEMS.register("goat_horn_pedestal",
            () -> new BlockItem(ModBlocks.GOAT_HORN_PEDESTAL.get(), new Item.Properties()));
    public static final RegistryObject<Item> SOUL_CALMING_LAMP = ITEMS.register("soul_calming_lamp",
            () -> new BlockItem(ModBlocks.SOUL_CALMING_LAMP.get(), new Item.Properties()));
    public static final RegistryObject<Item> TRIANGLE_STONE_BRICKS = ITEMS.register("triangle_stone_bricks",
            () -> new BlockItem(ModBlocks.TRIANGLE_STONE_BRICKS.get(), new Item.Properties()));
    public static final RegistryObject<Item> TRIANGLE_RUNE_TILES = ITEMS.register("triangle_rune_tiles",
            () -> new BlockItem(ModBlocks.TRIANGLE_RUNE_TILES.get(), new Item.Properties()));
    public static final RegistryObject<Item> QI_SAPPING_TRIANGLE_TILE = ITEMS.register("qi_sapping_triangle_tile",
            () -> new BlockItem(ModBlocks.QI_SAPPING_TRIANGLE_TILE.get(), new Item.Properties()));
    public static final RegistryObject<Item> CRACKED_TRIANGLE_STONE_BRICKS = ITEMS.register("cracked_triangle_stone_bricks",
            () -> new BlockItem(ModBlocks.CRACKED_TRIANGLE_STONE_BRICKS.get(), new Item.Properties()));
    public static final RegistryObject<Item> TRIANGLE_CORE_PILLAR = ITEMS.register("triangle_core_pillar",
            () -> new BlockItem(ModBlocks.TRIANGLE_CORE_PILLAR.get(), new Item.Properties()));
    public static final RegistryObject<Item> CHOPPING_STUMP = ITEMS.register("chopping_stump",
            () -> new BlockItem(ModBlocks.CHOPPING_STUMP.get(), new Item.Properties()));
    public static final RegistryObject<Item> CLEAVER_RACK = ITEMS.register("cleaver_rack",
            () -> new BlockItem(ModBlocks.CLEAVER_RACK.get(), new Item.Properties()));
    public static final RegistryObject<Item> ABANDONED_STABLE = ITEMS.register("abandoned_stable",
            () -> new BlockItem(ModBlocks.ABANDONED_STABLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> FROZEN_OBSERVATORY = ITEMS.register("frozen_observatory",
            () -> new BlockItem(ModBlocks.FROZEN_OBSERVATORY.get(), new Item.Properties()));
    public static final RegistryObject<Item> ABANDONED_KENNEL = ITEMS.register("abandoned_kennel",
            () -> new BlockItem(ModBlocks.ABANDONED_KENNEL.get(), new Item.Properties()));
    public static final RegistryObject<Item> AZURE_SEAL_CHAIN = ITEMS.register("azure_seal_chain",
            () -> new BlockItem(ModBlocks.AZURE_SEAL_CHAIN.get(), new Item.Properties()));
    public static final RegistryObject<Item> AZURE_SOUL_CONTAINER = ITEMS.register("azure_soul_container",
            () -> new BlockItem(ModBlocks.AZURE_SOUL_CONTAINER.get(), new Item.Properties()));
    public static final RegistryObject<Item> AZURE_BUTCHER_SPAWN_RUNE = ITEMS.register("azure_butcher_spawn_rune",
            () -> new BlockItem(ModBlocks.AZURE_BUTCHER_SPAWN_RUNE.get(), new Item.Properties()));
    public static final RegistryObject<Item> STAR_MANE_PEGASUS_SPAWN_EGG = ITEMS.register("star_mane_pegasus_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.STAR_MANE_PEGASUS, 0xD8C47A, 0x74D8FF, new Item.Properties()));
    public static final RegistryObject<Item> FROST_SHELL_SILVERFISH_SPAWN_EGG = ITEMS.register("frost_shell_silverfish_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.FROST_SHELL_SILVERFISH, 0x7EC8FF, 0xE8F8FF, new Item.Properties()));
    public static final RegistryObject<Item> BLACK_MANE_HOUND_SPAWN_EGG = ITEMS.register("black_mane_hound_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BLACK_MANE_HOUND, 0x1C1C1C, 0x7A3F2A, new Item.Properties()));
    public static final RegistryObject<Item> CULTIST_ECHO_SPAWN_EGG = ITEMS.register("cultist_echo_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CULTIST_ECHO, 0x2A2440, 0x9E79FF, new Item.Properties()));
    public static final RegistryObject<Item> ANCIENT_CULTIST_SPAWN_EGG = ITEMS.register("ancient_cultist_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ANCIENT_CULTIST, 0x3A3048, 0xB89C62, new Item.Properties()));
    public static final RegistryObject<Item> ALTAR_CULTIST_SPAWN_EGG = ITEMS.register("altar_cultist_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ALTAR_CULTIST, 0x241A32, 0xE0B85A, new Item.Properties()));
    public static final RegistryObject<Item> GOAT_HUNTER_BUTCHER_SPAWN_EGG = ITEMS.register("goat_hunter_butcher_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.GOAT_HUNTER_BUTCHER, 0x4A1A1A, 0xD0C0A0, new Item.Properties()));
    public static final RegistryObject<Item> TORMENTED_WRAITH_SPAWN_EGG = ITEMS.register("tormented_wraith_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TORMENTED_WRAITH, 0x283044, 0xA7F3FF, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
