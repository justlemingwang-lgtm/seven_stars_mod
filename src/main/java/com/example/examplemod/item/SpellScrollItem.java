package com.example.examplemod.item;

import com.example.examplemod.skill.SkillManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpellScrollItem extends Item {
    private final String skillId;

    public SpellScrollItem(String skillId, Properties properties) {
        super(properties);
        this.skillId = skillId;
    }

    public String getSkillId() {
        return skillId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            SkillManager.unlockWithScroll(serverPlayer, skillId, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
