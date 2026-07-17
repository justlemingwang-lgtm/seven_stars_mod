package com.example.examplemod.block;

import com.example.examplemod.entity.CultistEchoEntity;
import com.example.examplemod.registry.ModEntities;
import com.example.examplemod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class GoatHornAltarBlock extends Block implements EntityBlock {
    public GoatHornAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof GoatHornAltarBlockEntity altar)) {
            return InteractionResult.PASS;
        }

        // TODO stage 2 structure flow: replace this creative shortcut with the altar-cultist defeat trigger.
        if (player.isCreative() && player.isShiftKeyDown()) {
            altar.debugUnlock();
            player.displayClientMessage(Component.translatable("message.sevenstars.goat_horn_altar_unlocked"), true);
            return InteractionResult.CONSUME;
        }

        if (!stack.is(ModItems.BLOODY_CLEAVER.get())) {
            if (altar.isAltarUnlocked() && !altar.isHornClaimed()) {
                giveOrDrop(player, new ItemStack(ModItems.COMPLETE_GOAT_HORN.get()));
                giveOrDrop(player, new ItemStack(ModItems.GOAT_HORN_CORE.get()));
                altar.markHornClaimed();
                player.displayClientMessage(Component.translatable("message.sevenstars.goat_horn_altar_claimed"), true);
                return InteractionResult.CONSUME;
            }
            player.displayClientMessage(Component.translatable(altar.isHornClaimed()
                    ? "message.sevenstars.goat_horn_altar_claimed_already"
                    : "message.sevenstars.goat_horn_altar_locked"), true);
            return InteractionResult.CONSUME;
        }

        if (!altar.canSummonEcho()) {
            player.displayClientMessage(Component.translatable("message.sevenstars.goat_horn_altar_spent"), true);
            return InteractionResult.CONSUME;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        CultistEchoEntity echo = new CultistEchoEntity(ModEntities.CULTIST_ECHO.get(), serverLevel);
        echo.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, player.getYRot() + 180.0F, 0.0F);
        echo.setAltarPos(pos);
        serverLevel.addFreshEntity(echo);
        altar.markEchoSummoned(echo.getUUID());
        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D, 32, 0.4D, 0.35D, 0.4D, 0.02D);
        serverLevel.playSound(null, pos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.0F, 0.75F);
        player.displayClientMessage(Component.translatable("message.sevenstars.goat_horn_altar_summon"), true);
        return InteractionResult.CONSUME;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GoatHornAltarBlockEntity(pos, state);
    }
}
