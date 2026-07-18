package com.example.examplemod.item;

import com.example.examplemod.worldgen.structure.SkyArenaStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/** A dedicated Eye-of-Ender-style locator that can only find the Seven Stars sky arena. */
public final class AzureDragonEyeItem extends Item {
    public static final int SEARCH_RADIUS_CHUNKS = 512;

    public AzureDragonEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(stack);
        }
        if (serverLevel.dimension() != Level.OVERWORLD) {
            player.displayClientMessage(Component.translatable("message.sevenstars.azure_dragon_eye.wrong_dimension"), true);
            return InteractionResultHolder.fail(stack);
        }

        BlockPos target = serverLevel.findNearestMapStructure(SkyArenaStructure.LOCATABLE_TAG,
                player.blockPosition(), SEARCH_RADIUS_CHUNKS, false);
        if (target == null) {
            player.displayClientMessage(Component.translatable("message.sevenstars.azure_dragon_eye.not_found"), true);
            return InteractionResultHolder.fail(stack);
        }

        EyeOfEnder eye = new EyeOfEnder(level, player.getX(), player.getY(0.5D), player.getZ());
        eye.setItem(stack);
        eye.signalTo(target);
        level.gameEvent(GameEvent.PROJECTILE_SHOOT, eye.position(), GameEvent.Context.of(player));
        level.addFreshEntity(eye);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_LAUNCH,
                SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        level.levelEvent(null, 1003, player.blockPosition(), 0);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        player.awardStat(Stats.ITEM_USED.get(this));
        player.swing(hand, true);
        return InteractionResultHolder.success(stack);
    }
}
