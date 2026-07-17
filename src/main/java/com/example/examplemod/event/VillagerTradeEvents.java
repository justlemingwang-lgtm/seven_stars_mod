package com.example.examplemod.event;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.registry.ModItems;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class VillagerTradeEvents {
    private static final int MASTER_LEVEL = 5;
    private static final int SEVEN_STAR_SCROLL_PRICE = 32;
    private static final int MAX_USES = 4;
    private static final int VILLAGER_XP = 30;
    private static final float PRICE_MULTIPLIER = 0.05F;

    private VillagerTradeEvents() {
    }

    @SubscribeEvent
    public static void addLibrarianTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.LIBRARIAN) {
            return;
        }

        event.getTrades().get(MASTER_LEVEL).add(new BasicItemListing(
                SEVEN_STAR_SCROLL_PRICE,
                new ItemStack(ModItems.SEVEN_STAR_SCROLL.get()),
                MAX_USES,
                VILLAGER_XP,
                PRICE_MULTIPLIER
        ));
    }
}
