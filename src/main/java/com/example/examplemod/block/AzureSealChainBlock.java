package com.example.examplemod.block;

import com.example.examplemod.stage3.Stage3Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class AzureSealChainBlock extends Block {
    public static final BooleanProperty BROKEN = BooleanProperty.create("broken");

    public AzureSealChainBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BROKEN, false));
    }

    public static boolean breakBySixMeridian(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof AzureSealChainBlock) || state.getValue(BROKEN)) return false;
        level.setBlock(pos, state.setValue(BROKEN, true), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.25F, 0.8F);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 24, 0.45D, 0.45D, 0.45D, 0.08D);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 14, 0.35D, 0.35D, 0.35D, 0.03D);
        findNearestContainer(level, pos);
        return true;
    }

    private static void findNearestContainer(ServerLevel level, BlockPos origin) {
        int radius = Stage3Constants.SEAL_SCAN_RADIUS;
        AzureSoulContainerBlockEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos scan : BlockPos.betweenClosed(origin.offset(-radius, -radius / 2, -radius),
                origin.offset(radius, radius / 2, radius))) {
            if (level.getBlockEntity(scan) instanceof AzureSoulContainerBlockEntity container) {
                double distance = scan.distSqr(origin);
                if (distance < best) {
                    best = distance;
                    nearest = container;
                }
            }
        }
        if (nearest != null) nearest.onSealChanged();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BROKEN);
    }
}
