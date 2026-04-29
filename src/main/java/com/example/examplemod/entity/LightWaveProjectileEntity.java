package com.example.examplemod.entity;

import com.example.examplemod.block.SoulCalmingLampBlock;
import com.example.examplemod.registry.ModBlocks;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.SkillManager;
import com.example.examplemod.stage2.SkillDisableManager;
import com.example.examplemod.stage2.SkillIds;
import com.example.examplemod.stage2.Stage2Constants;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LightWaveProjectileEntity extends ThrowableItemProjectile {
    private float damage = 4.0F;
    private double maxRange = 12.0D;
    private double startX;
    private double startY;
    private double startZ;

    public LightWaveProjectileEntity(EntityType<? extends LightWaveProjectileEntity> type, Level level) {
        super(type, level);
    }

    public void setSkillValues(float damage, double maxRange, int ignoredDrain) {
        this.damage = damage;
        this.maxRange = maxRange;
        this.startX = getX();
        this.startY = getY();
        this.startZ = getZ();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SPELL_FRAGMENT.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide() && result.getEntity() instanceof LivingEntity target && result.getEntity() != getOwner()) {
            float appliedDamage = target instanceof TormentedWraithEntity wraith && wraith.isManifested()
                    ? damage * Stage2Constants.WRAITH_LIGHTWAVE_DAMAGE_MULTIPLIER
                    : damage;
            if (getOwner() instanceof ServerPlayer caster) {
                SkillManager.applySkillDamage(caster, target, appliedDamage);
            } else {
                target.hurt(damageSources().magic(), appliedDamage);
            }
            if (target instanceof Player player && target.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.TRIANGLE_ARMOR.get())) {
                SkillDisableManager.disableSkill(player, SkillIds.TRIANGLE, Stage2Constants.TRIANGLE_DISABLE_TICKS_BY_LIGHTWAVE);
            }
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide() && result instanceof BlockHitResult blockHitResult) {
            tryActivateSoulLamp(blockHitResult);
            discard();
        }
    }

    private void tryActivateSoulLamp(BlockHitResult result) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockState state = serverLevel.getBlockState(result.getBlockPos());
        if (!state.is(ModBlocks.SOUL_CALMING_LAMP.get()) || !state.hasProperty(SoulCalmingLampBlock.ACTIVE)
                || state.getValue(SoulCalmingLampBlock.ACTIVE)) {
            return;
        }
        serverLevel.setBlock(result.getBlockPos(), state.setValue(SoulCalmingLampBlock.ACTIVE, true), 3);
        serverLevel.scheduleTick(result.getBlockPos(), ModBlocks.SOUL_CALMING_LAMP.get(), Stage2Constants.SOUL_LAMP_ACTIVE_TICKS);
        serverLevel.playSound(null, result.getBlockPos(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.8F, 1.35F);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                result.getBlockPos().getX() + 0.5D, result.getBlockPos().getY() + 0.75D, result.getBlockPos().getZ() + 0.5D,
                18, 0.25D, 0.25D, 0.25D, 0.02D);
        serverLevel.sendParticles(ParticleTypes.END_ROD,
                result.getBlockPos().getX() + 0.5D, result.getBlockPos().getY() + 0.9D, result.getBlockPos().getZ() + 0.5D,
                8, 0.16D, 0.18D, 0.16D, 0.01D);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && distanceToSqr(startX, startY, startZ) > maxRange * maxRange) {
            discard();
        }
    }
}
