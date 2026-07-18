package com.example.examplemod.registry;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExampleMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAGIC_TAB = TABS.register("magic_tab", () -> CreativeModeTab.builder()
            .icon(() -> ModItems.ABSORBER.get().getDefaultInstance())
            .title(Component.translatable("creativetab.sevenstars.magic_tab"))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.ABSORBER.get());
                output.accept(ModItems.CONSUMER.get());
                output.accept(ModItems.SEVEN_STARS_CODEX.get());
                output.accept(ModItems.AZURE_DRAGON_SCALE.get());
                output.accept(ModItems.AZURE_DRAGON_EYE.get());
                output.accept(ModItems.LOST_STAR_MAGIC_TOKEN.get());
                output.accept(ModItems.AZURE_SEAL_CHAIN.get());
                output.accept(ModItems.AZURE_SOUL_CONTAINER.get());
                output.accept(ModItems.AZURE_BUTCHER_SPAWN_RUNE.get());
                output.accept(ModItems.SPELL_FRAGMENT.get());
                output.accept(ModItems.QI_CRYSTAL_SHARD.get());
                output.accept(ModItems.QI_CRYSTAL_CANDY.get());
                output.accept(ModItems.STAR_MANE_APPLE.get());
                output.accept(ModItems.FROST_CRYSTAL_CAKE_SLICE.get());
                output.accept(ModItems.HOUND_JERKY.get());
                output.accept(ModItems.SEVEN_STAR_SCROLL.get());
                output.accept(ModItems.QI_HELMET.get());
                output.accept(ModItems.COOLDOWN_CHESTPLATE.get());
                output.accept(ModItems.GOAT_HORN_ARMOR.get());
                output.accept(ModItems.TRIANGLE_ARMOR.get());
                output.accept(ModItems.QI_CRYSTAL_ORE.get());
                output.accept(ModItems.DEEPSLATE_QI_CRYSTAL_ORE.get());
                output.accept(ModItems.QI_CRYSTAL_BLOCK.get());
                output.accept(ModItems.QI_CRYSTAL_LAMP.get());
                output.accept(ModItems.QI_CRYSTAL_BRICKS.get());
                output.accept(ModItems.STAR_HOOF_CHARM_BLOCK.get());
                output.accept(ModItems.FROST_MARROW_CANDLE.get());
                output.accept(ModItems.CLAW_TROPHY_RACK.get());
                output.accept(ModItems.STAR_CURSED_BRICKS.get());
                output.accept(ModItems.CRACKED_STAR_CURSED_BRICKS.get());
                output.accept(ModItems.STAR_RUNE_TILES.get());
                output.accept(ModItems.GOAT_HORN_ALTAR.get());
                output.accept(ModItems.GOAT_HORN_PEDESTAL.get());
                output.accept(ModItems.SOUL_CALMING_LAMP.get());
                output.accept(ModItems.TRIANGLE_STONE_BRICKS.get());
                output.accept(ModItems.TRIANGLE_RUNE_TILES.get());
                output.accept(ModItems.QI_SAPPING_TRIANGLE_TILE.get());
                output.accept(ModItems.CRACKED_TRIANGLE_STONE_BRICKS.get());
                output.accept(ModItems.TRIANGLE_CORE_PILLAR.get());
                output.accept(ModItems.CHOPPING_STUMP.get());
                output.accept(ModItems.CLEAVER_RACK.get());
                output.accept(ModItems.COMPLETE_GOAT_HORN.get());
                output.accept(ModItems.GOAT_HORN_CORE.get());
                output.accept(ModItems.ANCIENT_CLOTH.get());
                output.accept(ModItems.STAR_RUNE_SHARD.get());
                output.accept(ModItems.ANCIENT_GOAT_PAGE.get());
                output.accept(ModItems.TRIANGLE_STONE_CIRCLE_PAGE.get());
                output.accept(ModItems.HUNTER_PAGE.get());
                output.accept(ModItems.GOAT_HORN_SPIKE_SCROLL_FRAGMENT.get());
                output.accept(ModItems.BLOODY_CLEAVER.get());
                output.accept(ModItems.SHIELDBREAKER_BONE.get());
                output.accept(ModItems.DULL_BLADE_FRAGMENT.get());
                output.accept(ModItems.CHOP_SCROLL.get());
                output.accept(ModItems.GOAT_HORN_SCROLL.get());
                output.accept(ModItems.TRIANGLE_FRAGMENT.get());
                output.accept(ModItems.TRIANGLE_CORE.get());
                output.accept(ModItems.TRIANGLE_SCROLL.get());
                output.accept(ModItems.SOUL_LAMP_WICK.get());
                output.accept(ModItems.PEGASUS_MANE.get());
                output.accept(ModItems.STAR_HOOF_FRAGMENT.get());
                output.accept(ModItems.COMPLETE_STAR_HOOF.get());
                output.accept(ModItems.FROST_SHELL_FRAGMENT.get());
                output.accept(ModItems.FROST_POWDER.get());
                output.accept(ModItems.FROST_MARROW_CRYSTAL.get());
                output.accept(ModItems.BLACK_MANE.get());
                output.accept(ModItems.CRACKED_CLAW_BONE.get());
                output.accept(ModItems.HOUND_FANG.get());
                output.accept(ModItems.STAR_REIN_BELL.get());
                output.accept(ModItems.STAR_HOOF_SPEAR.get());
                output.accept(ModItems.FROST_MARROW_WAND.get());
                output.accept(ModItems.CRACKED_CLAW_DAGGER.get());
                output.accept(ModItems.LIGHT_WAVE_SCROLL.get());
                output.accept(ModItems.SEVEN_STAR_SKILL_SCROLL.get());
                output.accept(ModItems.SIX_MERIDIAN_SWORD_SCROLL.get());
                output.accept(ModItems.GOLDEN_FINGER_SCROLL.get());
                output.accept(ModItems.PEGASUS_STEP_SCROLL.get());
                output.accept(ModItems.ICE_DIPPER_SHOT_SCROLL.get());
                output.accept(ModItems.HOUND_CLAW_SCROLL.get());
                output.accept(ModItems.WRITING_TABLE.get());
                output.accept(ModItems.ABANDONED_STABLE.get());
                output.accept(ModItems.FROZEN_OBSERVATORY.get());
                output.accept(ModItems.ABANDONED_KENNEL.get());
                output.accept(ModItems.STAR_MANE_PEGASUS_SPAWN_EGG.get());
                output.accept(ModItems.FROST_SHELL_SILVERFISH_SPAWN_EGG.get());
                output.accept(ModItems.BLACK_MANE_HOUND_SPAWN_EGG.get());
                output.accept(ModItems.CULTIST_ECHO_SPAWN_EGG.get());
                output.accept(ModItems.ANCIENT_CULTIST_SPAWN_EGG.get());
                output.accept(ModItems.ALTAR_CULTIST_SPAWN_EGG.get());
                output.accept(ModItems.GOAT_HUNTER_BUTCHER_SPAWN_EGG.get());
                output.accept(ModItems.TORMENTED_WRAITH_SPAWN_EGG.get());
                acceptPotionSet(output, ModPotions.QI_SURGE.get());
                acceptPotionSet(output, ModPotions.STRONG_QI_SURGE.get());
                acceptPotionSet(output, ModPotions.LONG_QI_SURGE.get());
                acceptPotionSet(output, ModPotions.QI_EXHAUSTION.get());
                acceptPotionSet(output, ModPotions.LONG_QI_EXHAUSTION.get());
            })
            .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

    public static void acceptPotionSet(CreativeModeTab.Output output, Potion potion) {
        output.accept(potionStack(Items.POTION, potion));
        output.accept(potionStack(Items.SPLASH_POTION, potion));
        output.accept(potionStack(Items.LINGERING_POTION, potion));
    }

    public static ItemStack potionStack(Item item, Potion potion) {
        return PotionUtils.setPotion(new ItemStack(item), potion);
    }
}
