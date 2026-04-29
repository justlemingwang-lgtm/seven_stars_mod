package com.example.examplemod.stage2;

import com.example.examplemod.entity.TriangleShardProjectileEntity;
import com.example.examplemod.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class SevenScatteredStrikesCaster {
    private SevenScatteredStrikesCaster() {
    }

    public static void cast(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int count = Stage2Constants.SEVEN_SCATTERED_STRIKES_PROJECTILE_COUNT;
        float center = (count - 1) / 2.0F;
        for (int i = 0; i < count; i++) {
            float yawOffset = (i - center) * Stage2Constants.SEVEN_SCATTERED_STRIKES_SPREAD_DEGREES;
            float pitchOffset = (player.getRandom().nextFloat() - 0.5F) * 5.0F;
            TriangleShardProjectileEntity projectile = new TriangleShardProjectileEntity(ModEntities.TRIANGLE_SHARD_PROJECTILE.get(), level);
            projectile.setOwner(player);
            projectile.setPos(player.getX(), player.getEyeY() - 0.12D, player.getZ());
            projectile.setSkillValues(Stage2Constants.SEVEN_SCATTERED_STRIKES_DAMAGE,
                    Stage2Constants.SEVEN_SCATTERED_STRIKES_RANGE,
                    Stage2Constants.SEVEN_SCATTERED_STRIKES_QI_DAMAGE);
            projectile.shootFromRotation(player, player.getXRot() + pitchOffset, player.getYRot() + yawOffset, 0.0F,
                    Stage2Constants.SEVEN_SCATTERED_STRIKES_SPEED,
                    Stage2Constants.SEVEN_SCATTERED_STRIKES_INACCURACY);
            level.addFreshEntity(projectile);
        }
    }
}
