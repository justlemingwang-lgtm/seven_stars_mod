package com.example.examplemod.client;

import com.example.examplemod.ClientConfig;
import com.example.examplemod.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public final class QinglongIllusionEffects {
    private QinglongIllusionEffects() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientArmorDisableData.tick();
        if (minecraft.player == null || minecraft.level == null) {
            ClientQinglongIllusion.clear();
            return;
        }
        ClientQinglongIllusion.tick();
        if (ClientQinglongIllusion.isActive()
                && minecraft.level.getEntity(ClientQinglongIllusion.getBossEntityId()) == null) {
            ClientQinglongIllusion.clear();
            return;
        }
        if (ClientQinglongIllusion.isActive() && minecraft.level.random.nextInt(3) == 0) {
            minecraft.level.addParticle(ParticleTypes.FLAME,
                    minecraft.player.getX() + (minecraft.level.random.nextDouble() - 0.5D) * 0.8D,
                    minecraft.player.getY() + minecraft.level.random.nextDouble() * 1.8D,
                    minecraft.player.getZ() + (minecraft.level.random.nextDouble() - 0.5D) * 0.8D,
                    0.0D, 0.025D, 0.0D);
        }
    }

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ClientQinglongIllusion.isActive() || minecraft.options.hideGui) return;
        int alpha = 16; // Approximately 15% opaque (38 / 255).
        event.getGuiGraphics().fill(0, 0, event.getWindow().getGuiScaledWidth(),
                event.getWindow().getGuiScaledHeight(), (alpha << 24) | 0xD13B08);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!ClientQinglongIllusion.isActive()) return;
        event.setRed(0.48F);
        event.setGreen(0.24F);
        event.setBlue(0.16F);
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        if (!ClientQinglongIllusion.isActive()) return;
        event.setNearPlaneDistance(3.0F);
        event.setFarPlaneDistance(48.0F);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        if (!ClientQinglongIllusion.isActive() || ClientConfig.DISABLE_QINGLONG_CAMERA_SHAKE.get()) return;
        double t = ClientQinglongIllusion.getVisualTicks() + event.getPartialTick();
        float scale = ClientConfig.QINGLONG_ILLUSION_SHAKE_SCALE.get().floatValue();
        event.setYaw(event.getYaw() + (float) Math.sin(t * 0.13D) * 0.7F * scale);
        event.setPitch(event.getPitch() + (float) Math.cos(t * 0.11D) * 0.45F * scale);
        event.setRoll(event.getRoll() + (float) Math.sin(t * 0.09D) * 0.8F * scale);
    }
}
