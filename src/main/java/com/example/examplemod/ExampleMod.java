package com.example.examplemod;

import com.example.examplemod.network.ModNetwork;
import com.example.examplemod.registry.ModBlocks;
import com.example.examplemod.registry.ModBlockEntities;
import com.example.examplemod.registry.ModCreativeTabs;
import com.example.examplemod.registry.ModEntities;
import com.example.examplemod.registry.ModEffects;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.registry.ModPotions;
import com.example.examplemod.registry.ModStructures;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "sevenstars";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public ExampleMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEffects.register(modEventBus);
        ModPotions.register(modEventBus);
        ModStructures.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(ModNetwork::register);
        event.enqueueWork(this::registerBrewingRecipes);

        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void registerBrewingRecipes() {
        addBrewingMix(Potions.AWKWARD, ModItems.QI_CRYSTAL_SHARD.get(), ModPotions.QI_SURGE.get());
        addBrewingMix(ModPotions.QI_SURGE.get(), Items.GLOWSTONE_DUST, ModPotions.STRONG_QI_SURGE.get());
        addBrewingMix(ModPotions.QI_SURGE.get(), Items.REDSTONE, ModPotions.LONG_QI_SURGE.get());
        addBrewingMix(Potions.AWKWARD, ModItems.FROST_POWDER.get(), ModPotions.QI_EXHAUSTION.get());
        addBrewingMix(ModPotions.QI_EXHAUSTION.get(), Items.REDSTONE, ModPotions.LONG_QI_EXHAUSTION.get());
    }

    private void addBrewingMix(Potion input, Item ingredient, Potion output) {
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), input)),
                Ingredient.of(ingredient),
                PotionUtils.setPotion(new ItemStack(Items.POTION), output));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            ModCreativeTabs.acceptPotionSet(event, ModPotions.QI_SURGE.get());
            ModCreativeTabs.acceptPotionSet(event, ModPotions.STRONG_QI_SURGE.get());
            ModCreativeTabs.acceptPotionSet(event, ModPotions.LONG_QI_SURGE.get());
            ModCreativeTabs.acceptPotionSet(event, ModPotions.QI_EXHAUSTION.get());
            ModCreativeTabs.acceptPotionSet(event, ModPotions.LONG_QI_EXHAUSTION.get());
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
