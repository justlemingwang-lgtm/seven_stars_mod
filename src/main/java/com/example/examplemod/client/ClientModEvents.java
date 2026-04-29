package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.client.renderer.SimpleMobRenderers;
import com.example.examplemod.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LIGHT_WAVE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.GOLDEN_FINGER_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.ICE_DIPPER_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.FROST_MOB_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.GOAT_HORN_SPIKE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.TRIANGLE_SHARD_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.STAR_MANE_PEGASUS.get(), SimpleMobRenderers.PegasusRenderer::new);
        event.registerEntityRenderer(ModEntities.FROST_SHELL_SILVERFISH.get(), SimpleMobRenderers.FrostSilverfishRenderer::new);
        event.registerEntityRenderer(ModEntities.BLACK_MANE_HOUND.get(), SimpleMobRenderers.HoundRenderer::new);
        event.registerEntityRenderer(ModEntities.CULTIST_ECHO.get(), SimpleMobRenderers.CultistEchoRenderer::new);
        event.registerEntityRenderer(ModEntities.GOAT_HUNTER_BUTCHER.get(), SimpleMobRenderers.ButcherRenderer::new);
        event.registerEntityRenderer(ModEntities.TORMENTED_WRAITH.get(), SimpleMobRenderers.WraithRenderer::new);
    }
}
