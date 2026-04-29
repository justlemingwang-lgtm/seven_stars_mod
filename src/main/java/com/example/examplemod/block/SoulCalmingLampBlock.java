package com.example.examplemod.block;

import com.example.examplemod.stage2.Stage2Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SoulCalmingLampBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public SoulCalmingLampBlock(BlockBehaviour.Properties properties) {
        super(properties.lightLevel(state -> state.getValue(ACTIVE) ? Stage2Constants.SOUL_LAMP_LIGHT_LEVEL : 0));
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE)) {
            level.setBlock(pos, state.setValue(ACTIVE, false), 3);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE) || random.nextInt(3) != 0) {
            return;
        }
        level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                pos.getX() + 0.5D, pos.getY() + 0.75D, pos.getZ() + 0.5D,
                0.0D, 0.015D, 0.0D);
    }
}
