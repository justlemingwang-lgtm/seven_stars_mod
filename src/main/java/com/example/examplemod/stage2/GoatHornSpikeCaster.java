package com.example.examplemod.stage2;

import com.example.examplemod.entity.GoatHornSpikeProjectileEntity;
import com.example.examplemod.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class GoatHornSpikeCaster {
    private GoatHornSpikeCaster() {
    }

    public static boolean cast(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        for (int i = 0; i < Stage2Constants.GOAT_HORN_SPIKES_COUNT; i++) {
            GoatHornSpikeProjectileEntity projectile = new GoatHornSpikeProjectileEntity(ModEntities.GOAT_HORN_SPIKE_PROJECTILE.get(), level);
            projectile.setOwner(player);
            projectile.setPos(player.getX(), player.getEyeY() - 0.15D, player.getZ());
            projectile.setSkillValues(Stage2Constants.GOAT_HORN_SPIKE_DAMAGE, Stage2Constants.GOAT_HORN_SPIKE_RANGE, Stage2Constants.GOAT_HORN_SPIKE_DRAIN_QI);
            float yaw = player.getYRot() + (player.getRandom().nextFloat() * 2.0F - 1.0F) * Stage2Constants.GOAT_HORN_SPIKE_SPREAD_DEGREES;
            float pitch = player.getXRot() + (player.getRandom().nextFloat() * 2.0F - 1.0F) * Stage2Constants.GOAT_HORN_SPIKE_SPREAD_DEGREES;
            projectile.shootFromRotation(player, pitch, yaw, 0.0F, Stage2Constants.GOAT_HORN_SPIKE_SPEED, Stage2Constants.GOAT_HORN_SPIKE_INACCURACY);
            level.addFreshEntity(projectile);
        }
        return true;
    }
}
