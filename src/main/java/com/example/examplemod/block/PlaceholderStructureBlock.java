package com.example.examplemod.block;

import com.example.examplemod.entity.BlackManeHoundEntity;
import com.example.examplemod.entity.FrostShellSilverfishEntity;
import com.example.examplemod.entity.StarManePegasusEntity;
import com.example.examplemod.registry.ModEntities;
import com.example.examplemod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PlaceholderStructureBlock extends Block {
    private final Kind kind;

    public PlaceholderStructureBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && !oldState.is(this) && level instanceof ServerLevel serverLevel) {
            generate(serverLevel, pos);
        }
    }

    private void generate(ServerLevel level, BlockPos origin) {
        Block log = kind.log;
        Block plank = kind.plank;
        Block fence = kind.fence;
        Block accent = kind.accent;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    set(level, origin.offset(x, 0, z), fence);
                } else if ((x + z) % 2 == 0) {
                    set(level, origin.offset(x, 0, z), plank);
                }
            }
        }
        set(level, origin.offset(-2, 1, -2), log);
        set(level, origin.offset(2, 1, -2), log);
        set(level, origin.offset(-2, 1, 2), log);
        set(level, origin.offset(2, 1, 2), log);
        set(level, origin.offset(0, 0, -1), accent);
        set(level, origin.offset(1, 0, 1), accent);
        level.setBlock(origin, Blocks.CHEST.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, kind.facing), 3);
        if (level.getBlockEntity(origin) instanceof ChestBlockEntity chest) {
            fillChest(chest);
        }
        spawnMobs(level, origin.above());
    }

    private void set(ServerLevel level, BlockPos pos, Block block) {
        if (level.isEmptyBlock(pos) || level.getBlockState(pos).is(this)) {
            level.setBlock(pos, block.defaultBlockState(), 3);
        }
    }

    private void fillChest(ChestBlockEntity chest) {
        switch (kind) {
            case STABLE -> {
                chest.setItem(0, new ItemStack(ModItems.PEGASUS_MANE.get(), 2));
                chest.setItem(1, new ItemStack(ModItems.STAR_HOOF_FRAGMENT.get()));
                chest.setItem(2, new ItemStack(ModItems.QI_CRYSTAL_SHARD.get(), 4));
                chest.setItem(3, new ItemStack(Items.GOLDEN_CARROT, 2));
                chest.setItem(4, new ItemStack(Items.SADDLE));
                chest.setItem(5, new ItemStack(ModItems.SPELL_FRAGMENT.get()));
                chest.setItem(6, new ItemStack(ModItems.PEGASUS_STEP_SCROLL.get()));
            }
            case OBSERVATORY -> {
                chest.setItem(0, new ItemStack(ModItems.FROST_SHELL_FRAGMENT.get(), 2));
                chest.setItem(1, new ItemStack(ModItems.FROST_POWDER.get(), 2));
                chest.setItem(2, new ItemStack(ModItems.FROST_MARROW_CRYSTAL.get()));
                chest.setItem(3, new ItemStack(ModItems.QI_CRYSTAL_SHARD.get(), 4));
                chest.setItem(4, new ItemStack(Items.LAPIS_LAZULI, 6));
                chest.setItem(5, new ItemStack(ModItems.SPELL_FRAGMENT.get()));
                chest.setItem(6, new ItemStack(ModItems.ICE_DIPPER_SHOT_SCROLL.get()));
            }
            case KENNEL -> {
                chest.setItem(0, new ItemStack(ModItems.BLACK_MANE.get(), 2));
                chest.setItem(1, new ItemStack(ModItems.CRACKED_CLAW_BONE.get()));
                chest.setItem(2, new ItemStack(ModItems.HOUND_FANG.get()));
                chest.setItem(3, new ItemStack(Items.BONE, 6));
                chest.setItem(4, new ItemStack(Items.LEATHER, 2));
                chest.setItem(5, new ItemStack(ModItems.QI_CRYSTAL_SHARD.get(), 4));
                chest.setItem(6, new ItemStack(ModItems.SPELL_FRAGMENT.get()));
                chest.setItem(7, new ItemStack(ModItems.HOUND_CLAW_SCROLL.get()));
            }
        }
    }

    private void spawnMobs(ServerLevel level, BlockPos pos) {
        switch (kind) {
            case STABLE -> spawn(level, ModEntities.STAR_MANE_PEGASUS.get(), pos);
            case OBSERVATORY -> {
                spawn(level, ModEntities.FROST_SHELL_SILVERFISH.get(), pos);
                spawn(level, ModEntities.FROST_SHELL_SILVERFISH.get(), pos.offset(1, 0, 0));
            }
            case KENNEL -> {
                spawn(level, ModEntities.BLACK_MANE_HOUND.get(), pos);
                spawn(level, ModEntities.BLACK_MANE_HOUND.get(), pos.offset(1, 0, 0));
            }
        }
    }

    private <T extends net.minecraft.world.entity.Mob> void spawn(ServerLevel level, EntityType<T> type, BlockPos pos) {
        T entity = type.create(level);
        if (entity != null) {
            entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(entity);
        }
    }

    public enum Kind {
        STABLE(Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_FENCE, Blocks.HAY_BLOCK, net.minecraft.core.Direction.SOUTH),
        OBSERVATORY(Blocks.BIRCH_LOG, Blocks.BIRCH_PLANKS, Blocks.BIRCH_FENCE, Blocks.BLUE_ICE, net.minecraft.core.Direction.SOUTH),
        KENNEL(Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_FENCE, Blocks.BONE_BLOCK, net.minecraft.core.Direction.SOUTH);

        private final Block log;
        private final Block plank;
        private final Block fence;
        private final Block accent;
        private final net.minecraft.core.Direction facing;

        Kind(Block log, Block plank, Block fence, Block accent, net.minecraft.core.Direction facing) {
            this.log = log;
            this.plank = plank;
            this.fence = fence;
            this.accent = accent;
            this.facing = facing;
        }
    }
}
