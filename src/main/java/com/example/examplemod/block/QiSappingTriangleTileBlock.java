package com.example.examplemod.block;

import com.example.examplemod.qi.QiManager;
import com.example.examplemod.stage2.Stage2Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class QiSappingTriangleTileBlock extends Block {
    private static final String NEXT_TRIGGER_TAG = "SevenStarsTriangleTrapNextTrigger";

    public QiSappingTriangleTileBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    Stage2Constants.TRIANGLE_TRAP_SLOW_DURATION,
                    Stage2Constants.TRIANGLE_TRAP_SLOW_AMPLIFIER, false, true));
            if (living instanceof ServerPlayer player) {
                long now = level.getGameTime();
                long nextTrigger = player.getPersistentData().getLong(NEXT_TRIGGER_TAG);
                if (now >= nextTrigger) {
                    QiManager.drainQi(player, Stage2Constants.TRIANGLE_TRAP_QI_DRAIN);
                    player.getPersistentData().putLong(NEXT_TRIGGER_TAG,
                            now + Stage2Constants.TRIANGLE_TRAP_TRIGGER_COOLDOWN);
                }
            }
        }
        super.stepOn(level, pos, state, entity);
    }
}
