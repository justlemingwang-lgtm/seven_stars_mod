package com.example.examplemod.stage2;

import com.example.examplemod.registry.ModItems;
import com.example.examplemod.skill.SkillManager;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public final class ChopSkillLogic {
    private static final DustParticleOptions CHOP_DUST = new DustParticleOptions(new Vector3f(0.55F, 0.05F, 0.03F), 0.8F);

    private ChopSkillLogic() {
    }

    public static int performChop(LivingEntity attacker, float damage) {
        return performChop(attacker, damage, Stage2Constants.CHOP_RANGE, Stage2Constants.CHOP_HALF_ANGLE_DEGREES,
                Stage2Constants.CHOP_GOAT_HORN_DISABLE_TICKS, Stage2Constants.CHOP_KNOCKBACK);
    }

    public static int performChop(LivingEntity attacker, float damage, double range, double halfAngleDegrees, int goatHornDisableTicks, double knockback) {
        if (!(attacker.level() instanceof ServerLevel level)) {
            return 0;
        }
        spawnChopArc(level, attacker, range, halfAngleDegrees);
        int hits = 0;
        for (LivingEntity target : findTargetsInCone(level, attacker, range, halfAngleDegrees)) {
            applyChopDamage(attacker, target, damage);
            applyKnockback(attacker, target, knockback);
            applyGoatHornDisableIfNeeded(target, goatHornDisableTicks);
            hits++;
        }
        return hits;
    }

    public static List<LivingEntity> findTargetsInCone(Level level, LivingEntity attacker, double range, double halfAngleDegrees) {
        Vec3 look = attacker.getLookAngle().normalize();
        double minDot = Math.cos(Math.toRadians(halfAngleDegrees));
        AABB box = attacker.getBoundingBox().inflate(range + 0.5D);
        return level.getEntitiesOfClass(LivingEntity.class, box, target -> {
            if (!target.isAlive() || target == attacker || !attacker.hasLineOfSight(target)) {
                return false;
            }
            Vec3 toTarget = target.position().subtract(attacker.position());
            double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
            if (horizontalDistance <= 0.01D || horizontalDistance > range || Math.abs(toTarget.y) > Stage2Constants.CHOP_VERTICAL_RANGE) {
                return false;
            }
            Vec3 horizontalDirection = new Vec3(toTarget.x, 0.0D, toTarget.z).normalize();
            Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z).normalize();
            return horizontalLook.dot(horizontalDirection) >= minDot;
        });
    }

    public static void applyChopDamage(LivingEntity attacker, LivingEntity target, float damage) {
        if (attacker instanceof ServerPlayer player) {
            SkillManager.applySkillDamage(player, target, damage);
        } else if (attacker instanceof Mob mob) {
            target.hurt(mob.damageSources().mobAttack(mob), damage);
        } else {
            target.hurt(attacker.damageSources().magic(), damage);
        }
    }

    public static void applyGoatHornDisableIfNeeded(LivingEntity target, int disableTicks) {
        if (target.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GOAT_HORN_ARMOR.get()) && target instanceof Player playerTarget
                && !SkillDisableManager.isSkillDisabled(playerTarget, SkillIds.GOAT_HORN)) {
            // Chop is targeted counterplay: it disables Goat Horn's Qi-loss protection without negating health damage.
            SkillDisableManager.disableSkill(playerTarget, SkillIds.GOAT_HORN, disableTicks);
        }
    }

    public static void spawnChopWarning(ServerLevel level, LivingEntity attacker) {
        spawnChopArc(level, attacker, Stage2Constants.CHOP_RANGE, Stage2Constants.CHOP_HALF_ANGLE_DEGREES);
    }

    private static void applyKnockback(LivingEntity attacker, LivingEntity target, double strength) {
        if (strength <= 0.0D) {
            return;
        }
        Vec3 direction = target.position().subtract(attacker.position());
        if (direction.horizontalDistanceSqr() <= 0.0001D) {
            direction = attacker.getLookAngle();
        }
        Vec3 normalized = new Vec3(direction.x, 0.0D, direction.z).normalize();
        target.push(normalized.x * strength, 0.08D, normalized.z * strength);
        target.hurtMarked = true;
    }

    private static void spawnChopArc(ServerLevel level, LivingEntity attacker, double range, double halfAngleDegrees) {
        Vec3 origin = attacker.position().add(0.0D, 0.08D, 0.0D);
        double baseYaw = Math.atan2(attacker.getLookAngle().z, attacker.getLookAngle().x);
        for (double angle = -halfAngleDegrees; angle <= halfAngleDegrees; angle += 7.5D) {
            double radians = baseYaw + Math.toRadians(angle);
            for (double distance = 1.0D; distance <= range; distance += 0.7D) {
                Vec3 pos = origin.add(Math.cos(radians) * distance, 0.0D, Math.sin(radians) * distance);
                level.sendParticles(CHOP_DUST, pos.x, pos.y, pos.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
            }
        }
        Vec3 front = attacker.getLookAngle().normalize();
        for (double distance = 0.8D; distance <= range; distance += 0.45D) {
            Vec3 pos = origin.add(front.x * distance, 0.0D, front.z * distance);
            level.sendParticles(ParticleTypes.CRIMSON_SPORE, pos.x, pos.y, pos.z, 1, 0.025D, 0.01D, 0.025D, 0.0D);
        }
    }
}
