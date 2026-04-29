package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SkillKeyMappings {
    public static final KeyMapping OPEN_SKILL_WHEEL = key("open_skill_wheel", GLFW.GLFW_KEY_Z);
    public static final KeyMapping CAST_SELECTED_SKILL = key("cast_selected_skill", GLFW.GLFW_KEY_R);
    public static final KeyMapping CYCLE_SKILL_RANK = key("cycle_skill_rank", GLFW.GLFW_KEY_X);

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SKILL_WHEEL);
        event.register(CAST_SELECTED_SKILL);
        event.register(CYCLE_SKILL_RANK);
    }

    private static KeyMapping key(String name, int key) {
        return new KeyMapping("key.sevenstars." + name, InputConstants.Type.KEYSYM, key, "key.categories.sevenstars");
    }
}
