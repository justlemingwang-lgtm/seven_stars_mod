package com.example.examplemod.event;

import com.example.examplemod.Config;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.qi.QiManager;
import com.example.examplemod.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.example.examplemod.stage3.ArmorDisableManager;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class QiHelmetEvents {
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getSlot() != EquipmentSlot.HEAD) {
            return;
        }
        boolean nowWearing = event.getTo().is(ModItems.QI_HELMET.get());
        boolean wasWearing = event.getFrom().is(ModItems.QI_HELMET.get());
        if (nowWearing && !wasWearing) {
            if (!ArmorDisableManager.isArmorDisabled(player)) {
                QiManager.setMaxQi(player, Config.qiHelmetMaxQi, true);
            }
        } else if (!nowWearing && wasWearing) {
            QiManager.setMaxQi(player, Config.getMaxQiDefaultInt(), false);
        }
    }
}
