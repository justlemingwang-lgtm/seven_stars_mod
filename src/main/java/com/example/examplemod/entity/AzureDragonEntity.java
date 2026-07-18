package com.example.examplemod.entity;

import com.example.examplemod.network.ModNetwork;
import com.example.examplemod.network.QinglongIllusionPacket;
import com.example.examplemod.registry.ModBlocks;
import com.example.examplemod.registry.ModEntities;
import com.example.examplemod.registry.ModItems;
import com.example.examplemod.stage2.SkillDisableManager;
import com.example.examplemod.stage2.SkillIds;
import com.example.examplemod.stage3.ArmorDisableManager;
import com.example.examplemod.stage3.AzureDragonAttackType;
import com.example.examplemod.stage3.Stage3Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.entity.PartEntity;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AzureDragonEntity extends Monster {
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(AzureDragonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TYPE = SynchedEntityData.defineId(AzureDragonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TICK = SynchedEntityData.defineId(AzureDragonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TRANSITIONING = SynchedEntityData.defineId(AzureDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DEFEATED = SynchedEntityData.defineId(AzureDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final DustParticleOptions AZURE_DUST = new DustParticleOptions(new Vector3f(0.05F, 0.85F, 1.0F), 1.25F);
    private static final DustParticleOptions WARNING_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.08F, 0.015F), 1.65F);

    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable("entity.sevenstars.azure_dragon"),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    private final Set<UUID> participants = new HashSet<>();
    private final Set<UUID> illusionPlayers = new HashSet<>();
    private BlockPos arenaCenter = BlockPos.ZERO;
    private int pendingPhase;
    private int transitionTicks;
    private int attackCooldown = Stage3Constants.ATTACK_INTERVAL_TICKS;
    private int butcherSummonTimer;
    private int defeatTicks;
    private float lockedAttackYaw;
    private float lockedAttackPitch;
    private AzureDragonAttackType lastAttack = AzureDragonAttackType.NONE;
    private Vec3 lockedTargetPosition = Vec3.ZERO;
    private boolean slamImpacted;
    private int orbitDirection = 1;
    private int orbitTicks;
    private final Set<UUID> attackVictims = new HashSet<>();
    public float previousFlapTime;
    public float flapTime;

    public final AzureDragonPart head;
    private final AzureDragonPart neck;
    private final AzureDragonPart body;
    private final AzureDragonPart tailOne;
    private final AzureDragonPart tailTwo;
    private final AzureDragonPart tailThree;
    private final AzureDragonPart leftWing;
    private final AzureDragonPart rightWing;
    private final AzureDragonPart[] dragonParts;

    public AzureDragonEntity(EntityType<? extends AzureDragonEntity> type, Level level) {
        super(type, level);
        head = new AzureDragonPart(this, "head", 2.2F, 2.2F);
        neck = new AzureDragonPart(this, "neck", 3.0F, 3.0F);
        body = new AzureDragonPart(this, "body", 5.0F, 3.6F);
        tailOne = new AzureDragonPart(this, "tail", 2.4F, 2.4F);
        tailTwo = new AzureDragonPart(this, "tail", 2.0F, 2.0F);
        tailThree = new AzureDragonPart(this, "tail", 1.6F, 1.6F);
        leftWing = new AzureDragonPart(this, "wing", 4.5F, 2.0F);
        rightWing = new AzureDragonPart(this, "wing", 4.5F, 2.0F);
        dragonParts = new AzureDragonPart[]{head, neck, body, tailOne, tailTwo, tailThree, leftWing, rightWing};
        setId(ENTITY_COUNTER.getAndAdd(dragonParts.length + 1) + 1);
        setPersistenceRequired();
        noCulling = true;
        xpReward = 100;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (dragonParts != null) {
            for (int index = 0; index < dragonParts.length; index++) {
                dragonParts[index].setId(id + index + 1);
            }
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return dragonParts;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Stage3Constants.AZURE_DRAGON_MAX_HEALTH)
                .add(Attributes.ARMOR, Stage3Constants.AZURE_DRAGON_ARMOR)
                .add(Attributes.ATTACK_DAMAGE, Stage3Constants.AZURE_DRAGON_ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, Stage3Constants.AZURE_DRAGON_MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, Stage3Constants.AZURE_DRAGON_FOLLOW_RANGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, Stage3Constants.AZURE_DRAGON_KNOCKBACK_RESISTANCE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PHASE, 1);
        entityData.define(ATTACK_TYPE, AzureDragonAttackType.NONE.ordinal());
        entityData.define(ATTACK_TICK, 0);
        entityData.define(TRANSITIONING, false);
        entityData.define(DEFEATED, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.75D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public int getPhase() {
        return Mth.clamp(entityData.get(PHASE), 1, 3);
    }

    public AzureDragonAttackType getAttackType() {
        return AzureDragonAttackType.byId(entityData.get(ATTACK_TYPE));
    }

    public int getAttackTick() {
        return entityData.get(ATTACK_TICK);
    }

    public boolean isTransitioning() {
        return entityData.get(TRANSITIONING);
    }

    public boolean isDefeated() {
        return entityData.get(DEFEATED);
    }

    public boolean hasWingBlades() {
        return getAttackType() == AzureDragonAttackType.BULL_AZURE_SLASH
                && getAttackTick() >= Stage3Constants.BULL_SLASH_WINDUP;
    }

    public void setArenaCenter(BlockPos center) {
        arenaCenter = center.immutable();
    }

    public BlockPos getArenaCenter() {
        return arenaCenter;
    }

    @Override
    public void tick() {
        super.tick();
        previousFlapTime = flapTime;
        double speed = getDeltaMovement().horizontalDistance();
        flapTime += 0.055F + (float) Math.min(0.09D, speed * 0.12D);
        updateDragonParts();
    }

    private void updateDragonParts() {
        Vec3 forward = directionFromYaw(getYRot());
        Vec3 side = new Vec3(forward.z, 0.0D, -forward.x);
        double bodyY = getY() + 1.8D;
        positionPart(body, forward.scale(-0.4D).add(0.0D, 0.1D, 0.0D));
        positionPart(neck, forward.scale(4.3D).add(0.0D, 1.4D, 0.0D));
        positionPart(head, forward.scale(6.7D).add(0.0D, 1.7D, 0.0D));
        positionPart(tailOne, forward.scale(-4.0D).add(0.0D, 1.0D, 0.0D));
        positionPart(tailTwo, forward.scale(-6.8D).add(0.0D, 0.8D, 0.0D));
        positionPart(tailThree, forward.scale(-9.4D).add(0.0D, 0.55D, 0.0D));
        positionPart(leftWing, side.scale(4.6D).add(0.0D, 1.5D, 0.0D));
        positionPart(rightWing, side.scale(-4.6D).add(0.0D, 1.5D, 0.0D));
        body.setPos(body.getX(), bodyY, body.getZ());
    }

    private void positionPart(AzureDragonPart part, Vec3 offset) {
        part.xo = part.getX();
        part.yo = part.getY();
        part.zo = part.getZ();
        part.xOld = part.xo;
        part.yOld = part.yo;
        part.zOld = part.zo;
        part.setPos(getX() + offset.x, getY() + offset.y, getZ() + offset.z);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            return;
        }
        if (arenaCenter.equals(BlockPos.ZERO) && !blockPosition().equals(BlockPos.ZERO)) {
            arenaCenter = blockPosition();
        }
        bossEvent.setProgress(getHealth() / getMaxHealth());
        if (tickCount % 10 == 0 || !isValidCombatTarget(getTarget())) {
            refreshCombatTarget();
        }
        if (isDefeated()) {
            tickDefeated();
            return;
        }
        if (isTransitioning()) {
            tickPhaseTransition();
            return;
        }
        if (getAttackType() != AzureDragonAttackType.NONE) {
            tickCurrentAttack();
        } else {
            LivingEntity target = getTarget();
            if (isValidCombatTarget(target)) {
                pursueTarget(target, 1.0D + getPhase() * 0.06D);
                if (attackCooldown-- <= 0) {
                    chooseNextAttack();
                }
            }
        }
        tickButcherSummoning();
        if (tickCount % 10 == 0) syncIllusionPlayers();
    }

    private void refreshCombatTarget() {
        LivingEntity current = getTarget();
        if (isValidCombatTarget(current)) return;
        if (!(level() instanceof ServerLevel serverLevel)) return;
        ServerPlayer nearest = serverLevel.players().stream()
                .filter(player -> player.isAlive() && !player.isCreative() && !player.isSpectator())
                .min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        setTarget(nearest);
    }

    private boolean isValidCombatTarget(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity.level() != level()) return false;
        return !(entity instanceof Player player) || (!player.isCreative() && !player.isSpectator());
    }

    private void pursueTarget(LivingEntity target, double speed) {
        getLookControl().setLookAt(target, 18.0F, 12.0F);
        Vec3 toTarget = target.position().subtract(position());
        Vec3 horizontalToTarget = new Vec3(toTarget.x, 0.0D, toTarget.z);
        if (horizontalToTarget.lengthSqr() > 0.01D) {
            float desiredYaw = (float) Math.toDegrees(Math.atan2(-horizontalToTarget.x, horizontalToTarget.z));
            setYRot(Mth.approachDegrees(getYRot(), desiredYaw, 12.0F));
            yBodyRot = getYRot();
        }
        double distance = distanceTo(target);
        if (distance > 4.25D) {
            getNavigation().moveTo(target, speed);
            applyChaseAcceleration(horizontalToTarget, speed);
            return;
        }
        if (--orbitTicks <= 0) {
            orbitTicks = 28 + random.nextInt(35);
            if (random.nextBoolean()) orbitDirection = -orbitDirection;
        }
        Vec3 delta = position().subtract(target.position());
        Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
        if (horizontal.lengthSqr() < 0.01D) horizontal = directionFromYaw(getYRot());
        horizontal = horizontal.normalize();
        Vec3 tangent = new Vec3(-horizontal.z * orbitDirection, 0.0D, horizontal.x * orbitDirection);
        Vec3 destination = target.position().add(horizontal.scale(3.0D)).add(tangent.scale(1.8D));
        getNavigation().moveTo(destination.x, destination.y, destination.z, speed * 0.9D);
        applyChaseAcceleration(destination.subtract(position()).multiply(1.0D, 0.0D, 1.0D), speed * 0.65D);
    }

    private void applyChaseAcceleration(Vec3 horizontalDirection, double speedMultiplier) {
        if (horizontalDirection.lengthSqr() < 0.01D || horizontalCollision) return;
        Vec3 direction = horizontalDirection.normalize();
        double desiredSpeed = getAttributeValue(Attributes.MOVEMENT_SPEED) * speedMultiplier;
        Vec3 motion = getDeltaMovement();
        double blend = getNavigation().isDone() ? 0.42D : 0.22D;
        setDeltaMovement(Mth.lerp(blend, motion.x, direction.x * desiredSpeed), motion.y,
                Mth.lerp(blend, motion.z, direction.z * desiredSpeed));
    }

    public void beginPhaseTransition(int nextPhase) {
        if (isTransitioning() || isDefeated() || nextPhase != getPhase() + 1 || nextPhase > 3) return;
        finishAttack();
        pendingPhase = nextPhase;
        transitionTicks = 0;
        entityData.set(TRANSITIONING, true);
        entityData.set(ATTACK_TYPE, AzureDragonAttackType.PHASE_TRANSITION.ordinal());
        getNavigation().stop();
        setInvulnerable(true);
        level().playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.0F, 0.7F);
    }

    public void tickPhaseTransition() {
        transitionTicks++;
        entityData.set(ATTACK_TICK, transitionTicks);
        if (level() instanceof ServerLevel serverLevel && transitionTicks % 3 == 0) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.8D, getZ(), 14,
                    2.5D, 1.8D, 2.5D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.8D, getZ(), 8,
                    2.0D, 1.5D, 2.0D, 0.12D);
        }
        if (transitionTicks >= Stage3Constants.PHASE_TRANSITION_TICKS) finishPhaseTransition();
    }

    public void finishPhaseTransition() {
        if (!isTransitioning()) return;
        int next = Mth.clamp(pendingPhase, getPhase() + 1, 3);
        entityData.set(PHASE, next);
        entityData.set(TRANSITIONING, false);
        entityData.set(ATTACK_TYPE, AzureDragonAttackType.NONE.ordinal());
        entityData.set(ATTACK_TICK, 0);
        pendingPhase = 0;
        transitionTicks = 0;
        setInvulnerable(false);
        attackCooldown = Stage3Constants.ATTACK_INTERVAL_TICKS;
        if (next == 2) onEnteredPhaseTwo();
        if (next == 3) onEnteredPhaseThree();
    }

    protected void onEnteredPhaseTwo() {
        level().playSound(null, blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 1.6F, 0.9F);
    }

    protected void onEnteredPhaseThree() {
        syncIllusionPlayers();
        level().playSound(null, blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 2.2F, 0.55F);
    }

    public void chooseNextAttack() {
        LivingEntity target = getTarget();
        if (!isValidCombatTarget(target)) return;
        double distance = distanceTo(target);
        List<AzureDragonAttackType> pool = new ArrayList<>();
        if (distance <= 9.0D) {
            addWeighted(pool, AzureDragonAttackType.BITE, 9);
            addWeighted(pool, AzureDragonAttackType.STOMP, 4);
            addWeighted(pool, AzureDragonAttackType.TURNING_TAIL, 4);
            addWeighted(pool, AzureDragonAttackType.PHYSICAL_BREATH, 2);
            if (getPhase() >= 2) {
                addWeighted(pool, AzureDragonAttackType.BULL_AZURE_SLASH, 9);
                addWeighted(pool, AzureDragonAttackType.DIVINE_TAIL, 5);
                addWeighted(pool, AzureDragonAttackType.AZURE_BREATH, 2);
            }
        } else if (distance <= 22.0D) {
            addWeighted(pool, AzureDragonAttackType.PHYSICAL_BREATH, 5);
            addWeighted(pool, AzureDragonAttackType.CHARGE, 5);
            addWeighted(pool, AzureDragonAttackType.AERIAL_SLAM, 3);
            addWeighted(pool, AzureDragonAttackType.BITE, 2);
            if (getPhase() >= 2) {
                addWeighted(pool, AzureDragonAttackType.BULL_AZURE_SLASH, 7);
                addWeighted(pool, AzureDragonAttackType.AZURE_BREATH, 6);
                addWeighted(pool, AzureDragonAttackType.DIVINE_TAIL, 2);
            }
        } else {
            addWeighted(pool, AzureDragonAttackType.CHARGE, 7);
            addWeighted(pool, AzureDragonAttackType.AERIAL_SLAM, 5);
            addWeighted(pool, AzureDragonAttackType.PHYSICAL_BREATH, 6);
            if (getPhase() >= 2) {
                addWeighted(pool, AzureDragonAttackType.AZURE_BREATH, 8);
                addWeighted(pool, AzureDragonAttackType.BULL_AZURE_SLASH, 3);
            }
        }
        if (pool.size() > 1) pool.removeIf(type -> type == lastAttack);
        AzureDragonAttackType selected = pool.get(random.nextInt(pool.size()));
        startAttack(selected);
    }

    private static void addWeighted(List<AzureDragonAttackType> pool, AzureDragonAttackType type, int weight) {
        for (int index = 0; index < weight; index++) pool.add(type);
    }

    public void startAttack(AzureDragonAttackType type) {
        if (type == AzureDragonAttackType.NONE || isTransitioning() || isDefeated()) return;
        entityData.set(ATTACK_TYPE, type.ordinal());
        entityData.set(ATTACK_TICK, 0);
        lockedAttackYaw = getYRot();
        lastAttack = type;
        LivingEntity target = getTarget();
        lockedTargetPosition = target == null ? position() : target.position();
        if (target != null && (type == AzureDragonAttackType.PHYSICAL_BREATH
                || type == AzureDragonAttackType.AZURE_BREATH
                || type == AzureDragonAttackType.CHARGE
                || type == AzureDragonAttackType.BULL_AZURE_SLASH)) {
            faceAttackTarget(target, 360.0F, true);
        }
        if (target != null && (type == AzureDragonAttackType.PHYSICAL_BREATH
                || type == AzureDragonAttackType.AZURE_BREATH)) {
            faceBreathPitch(target, 360.0F);
        } else {
            lockedAttackPitch = 0.0F;
            setXRot(0.0F);
        }
        slamImpacted = false;
        attackVictims.clear();
        setNoGravity(type == AzureDragonAttackType.AERIAL_SLAM);
        setGlowingTag(true);
        if (type == AzureDragonAttackType.AERIAL_SLAM || type == AzureDragonAttackType.CHARGE) {
            getNavigation().stop();
        } else if (target != null) {
            pursueTarget(target, type == AzureDragonAttackType.BITE ? 1.25D : 0.85D);
        }
        if (!level().isClientSide()) {
            var sound = switch (type) {
                case PHYSICAL_BREATH, AZURE_BREATH -> SoundEvents.ENDER_DRAGON_SHOOT;
                case STOMP -> SoundEvents.WARDEN_SONIC_BOOM;
                case TURNING_TAIL, DIVINE_TAIL, BULL_AZURE_SLASH -> SoundEvents.PLAYER_ATTACK_SWEEP;
                case AERIAL_SLAM -> SoundEvents.ENDER_DRAGON_FLAP;
                case CHARGE -> SoundEvents.RAVAGER_ROAR;
                case BITE -> SoundEvents.RAVAGER_ATTACK;
                default -> SoundEvents.ENDER_DRAGON_GROWL;
            };
            level().playSound(null, blockPosition(), sound, SoundSource.HOSTILE, 1.4F, 0.9F);
            announceTelegraph(type);
        }
    }

    private void announceTelegraph(AzureDragonAttackType type) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        String key = "message.sevenstars.azure_dragon.telegraph." + type.name().toLowerCase(java.util.Locale.ROOT);
        for (ServerPlayer player : serverLevel.players()) {
            if (player != getTarget() && distanceToSqr(player) > Stage3Constants.AZURE_DRAGON_FOLLOW_RANGE
                    * Stage3Constants.AZURE_DRAGON_FOLLOW_RANGE) continue;
            player.displayClientMessage(Component.translatable(key), true);
            if (type == AzureDragonAttackType.AZURE_BREATH) {
                player.displayClientMessage(Component.translatable(key), false);
            }
        }
    }

    public void tickCurrentAttack() {
        AzureDragonAttackType type = getAttackType();
        int tick = getAttackTick() + 1;
        entityData.set(ATTACK_TICK, tick);
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            finishAttack();
            return;
        }
        if (type != AzureDragonAttackType.AERIAL_SLAM && type != AzureDragonAttackType.CHARGE
                && type != AzureDragonAttackType.BULL_AZURE_SLASH) {
            double chaseSpeed = type == AzureDragonAttackType.BITE ? 1.3D
                    : type == AzureDragonAttackType.PHYSICAL_BREATH || type == AzureDragonAttackType.AZURE_BREATH
                    ? 0.52D : 0.72D;
            pursueTarget(target, chaseSpeed);
        }
        if (tick < windup(type)) {
            getLookControl().setLookAt(target, 12.0F, 10.0F);
            if (type == AzureDragonAttackType.CHARGE) {
                faceAttackTarget(target, 24.0F, true);
            } else if (type == AzureDragonAttackType.BULL_AZURE_SLASH) {
                faceAttackTarget(target, 32.0F, true);
            } else if (type == AzureDragonAttackType.PHYSICAL_BREATH
                    || type == AzureDragonAttackType.AZURE_BREATH) {
                faceAttackTarget(target, 14.0F, false);
                faceBreathPitch(target, 12.0F);
            } else {
                faceAttackTarget(target, 12.0F, false);
            }
            if (type != AzureDragonAttackType.AERIAL_SLAM) lockedTargetPosition = target.position();
            spawnWarning(type, tick);
            if (type == AzureDragonAttackType.AZURE_BREATH
                    && tick == Math.max(1, Stage3Constants.AZURE_BREATH_WINDUP - 5)) {
                announceAzureBreathReleaseWarning();
            }
            if (type == AzureDragonAttackType.BULL_AZURE_SLASH) {
                pursueTarget(target, 0.58D);
            }
        }
        switch (type) {
            case PHYSICAL_BREATH -> {
                int activeTick = tick - Stage3Constants.PHYSICAL_BREATH_WINDUP;
                if (activeTick >= 0 && activeTick < Stage3Constants.PHYSICAL_BREATH_ACTIVE) {
                    tickBreath(target, false, activeTick);
                }
            }
            case STOMP -> {
                if (tick == Stage3Constants.STOMP_WINDUP) hitRadius(Stage3Constants.STOMP_RADIUS,
                        Stage3Constants.STOMP_DAMAGE, false, false);
            }
            case TURNING_TAIL -> {
                if (tick == Stage3Constants.TURNING_TAIL_WINDUP) hitTail(Stage3Constants.TURNING_TAIL_RANGE,
                        Stage3Constants.TURNING_TAIL_DAMAGE, false);
            }
            case AZURE_BREATH -> {
                int activeTick = tick - Stage3Constants.AZURE_BREATH_WINDUP;
                if (activeTick >= 0 && activeTick < Stage3Constants.AZURE_BREATH_ACTIVE) {
                    tickBreath(target, true, activeTick);
                }
            }
            case DIVINE_TAIL -> {
                if (tick == Stage3Constants.DIVINE_TAIL_WINDUP) hitTail(Stage3Constants.DIVINE_TAIL_RANGE,
                        Stage3Constants.DIVINE_TAIL_DAMAGE, true);
            }
            case BULL_AZURE_SLASH -> tickBullSlash(tick, target);
            case AERIAL_SLAM -> tickAerialSlam(tick, target);
            case CHARGE -> tickCharge(tick);
            case BITE -> {
                if (tick == Stage3Constants.BITE_WINDUP) {
                    hitCone(false, Stage3Constants.BITE_RANGE, Stage3Constants.BITE_HALF_ANGLE,
                            Stage3Constants.BITE_DAMAGE, false);
                    level().playSound(null, blockPosition(), SoundEvents.FOX_BITE,
                            SoundSource.HOSTILE, 1.5F, 0.72F);
                }
            }
            default -> {
            }
        }
        if (tick >= totalDuration(type)) finishAttack();
    }

    private void tickBreath(LivingEntity target, boolean azure, int activeTick) {
        // The aim follows a smoothed historic position rather than the live player position.
        // This creates a short tracking delay so sustained lateral movement can evade the cone.
        if (activeTick == 0) {
            // Release always starts exactly on the current target. Delayed tracking begins afterwards.
            lockedTargetPosition = target.position();
        } else {
            lockedTargetPosition = lockedTargetPosition.lerp(target.position(), azure ? 0.065D : 0.075D);
        }
        Vec3 aimDelta = lockedTargetPosition.subtract(position());
        float desiredYaw = (float) Math.toDegrees(Math.atan2(-aimDelta.x, aimDelta.z));
        Vec3 mouth = breathMouthPosition();
        Vec3 verticalAim = lockedTargetPosition.add(0.0D, target.getBbHeight() * 0.55D, 0.0D).subtract(mouth);
        float desiredPitch = (float) -Math.toDegrees(Math.atan2(verticalAim.y, verticalAim.horizontalDistance()));
        lockedAttackYaw = activeTick == 0 ? desiredYaw
                : Mth.approachDegrees(lockedAttackYaw, desiredYaw, azure ? 1.1F : 1.35F);
        lockedAttackPitch = activeTick == 0 ? desiredPitch
                : Mth.approachDegrees(lockedAttackPitch, desiredPitch, azure ? 0.8F : 1.0F);
        // Keep visual head/body direction identical to the actual cone and particle stream.
        setYRot(lockedAttackYaw);
        yBodyRot = lockedAttackYaw;
        setYHeadRot(lockedAttackYaw);
        setXRot(lockedAttackPitch);
        spawnBreathStream(azure);
        int interval = azure ? Stage3Constants.AZURE_BREATH_PULSE_INTERVAL
                : Stage3Constants.PHYSICAL_BREATH_PULSE_INTERVAL;
        if (activeTick % interval == 0) {
            hitBreathCone(azure, azure ? Stage3Constants.AZURE_BREATH_RANGE : Stage3Constants.PHYSICAL_BREATH_RANGE,
                    azure ? Stage3Constants.AZURE_BREATH_HALF_ANGLE : Stage3Constants.PHYSICAL_BREATH_HALF_ANGLE,
                    azure ? Stage3Constants.AZURE_BREATH_DAMAGE : Stage3Constants.PHYSICAL_BREATH_DAMAGE,
                    azure);
        }
    }

    private void spawnBreathStream(boolean azure) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        double range = azure ? Stage3Constants.AZURE_BREATH_RANGE : Stage3Constants.PHYSICAL_BREATH_RANGE;
        Vec3 direction = directionFromRotation(lockedAttackYaw, lockedAttackPitch);
        Vec3 mouth = breathMouthPosition();
        for (double distance = 0.0D; distance <= range; distance += 3.0D) {
            Vec3 point = mouth.add(direction.scale(distance));
            double spread = 0.18D + distance * 0.055D;
            serverLevel.sendParticles(azure ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                    point.x, point.y, point.z, 1, spread, spread * 0.35D, spread, 0.015D);
            if (((int) distance) % 6 == 0) {
                serverLevel.sendParticles(azure ? AZURE_DUST : ParticleTypes.LARGE_SMOKE,
                        point.x, point.y, point.z, 1, spread * 0.7D, spread * 0.25D, spread * 0.7D, 0.01D);
            }
        }
    }

    private void tickBullSlash(int tick, LivingEntity target) {
        int chaseStart = Stage3Constants.BULL_SLASH_WINDUP;
        int chaseEnd = chaseStart + Stage3Constants.BULL_SLASH_CHASE;
        faceAttackTarget(target, 32.0F, true);
        if (tick >= chaseStart && tick < chaseEnd) {
            getNavigation().moveTo(target, 1.35D);
            applyChaseAcceleration(target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D), 1.35D);
            int relative = tick - chaseStart;
            if (relative % Stage3Constants.BULL_SLASH_INTERVAL == 0
                    && relative / Stage3Constants.BULL_SLASH_INTERVAL < Stage3Constants.BULL_SLASH_COUNT) {
                faceAttackTarget(target, 360.0F, true);
                hitRadius(Stage3Constants.BULL_SLASH_RANGE, Stage3Constants.BULL_SLASH_DAMAGE, true, true);
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(AZURE_DUST, getX(), getY() + 1.5D, getZ(), 20,
                            2.0D, 1.0D, 2.0D, 0.1D);
                }
            }
        } else if (tick >= chaseEnd) {
            pursueTarget(target, 0.8D);
        }
    }

    private void tickAerialSlam(int tick, LivingEntity target) {
        int riseEnd = Stage3Constants.AERIAL_SLAM_WINDUP - 12;
        if (tick <= riseEnd) {
            lockedTargetPosition = target.position();
            Vec3 horizontal = lockedTargetPosition.subtract(position()).multiply(0.06D, 0.0D, 0.06D);
            double desiredY = lockedTargetPosition.y + Stage3Constants.AERIAL_SLAM_HEIGHT;
            double vertical = Mth.clamp((desiredY - getY()) * 0.16D, 0.12D, 0.62D);
            setDeltaMovement(horizontal.x, vertical, horizontal.z);
        } else if (tick < Stage3Constants.AERIAL_SLAM_WINDUP) {
            setDeltaMovement(getDeltaMovement().multiply(0.45D, 0.15D, 0.45D));
        } else if (!slamImpacted) {
            Vec3 delta = lockedTargetPosition.subtract(position());
            Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
            Vec3 dive = horizontal.lengthSqr() < 0.01D ? Vec3.ZERO : horizontal.normalize().scale(1.05D);
            setDeltaMovement(dive.x, -1.45D, dive.z);
            if (onGround() || getY() <= lockedTargetPosition.y + 0.8D
                    || tick >= Stage3Constants.AERIAL_SLAM_WINDUP + Stage3Constants.AERIAL_SLAM_ACTIVE - 1) {
                slamImpacted = true;
                setNoGravity(false);
                setDeltaMovement(Vec3.ZERO);
                hitRadius(Stage3Constants.AERIAL_SLAM_RADIUS, Stage3Constants.AERIAL_SLAM_DAMAGE, false, false);
                spawnSlamImpact();
            }
        }
    }

    private void spawnSlamImpact() {
        level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.0F, 0.65F);
        if (!(level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 0.4D, getZ(), 1,
                0.0D, 0.0D, 0.0D, 0.0D);
        serverLevel.sendParticles(AZURE_DUST, getX(), getY() + 0.3D, getZ(), 90,
                Stage3Constants.AERIAL_SLAM_RADIUS * 0.55D, 0.35D,
                Stage3Constants.AERIAL_SLAM_RADIUS * 0.55D, 0.12D);
    }

    private void tickCharge(int tick) {
        int active = tick - Stage3Constants.CHARGE_WINDUP;
        if (active < 0) {
            setDeltaMovement(getDeltaMovement().multiply(0.25D, 1.0D, 0.25D));
            return;
        }
        if (active < Stage3Constants.CHARGE_ACTIVE) {
            Vec3 direction = directionFromYaw(lockedAttackYaw);
            setYRot(lockedAttackYaw);
            yBodyRot = lockedAttackYaw;
            setYHeadRot(lockedAttackYaw);
            setDeltaMovement(direction.x * Stage3Constants.CHARGE_SPEED, getDeltaMovement().y,
                    direction.z * Stage3Constants.CHARGE_SPEED);
            AABB hitBox = getBoundingBox().expandTowards(direction.scale(2.0D)).inflate(2.2D, 1.2D, 2.2D);
            for (Player player : level().getEntitiesOfClass(Player.class, hitBox,
                    player -> player.isAlive() && !player.isCreative() && !player.isSpectator())) {
                if (attackVictims.add(player.getUUID())) {
                    hurtPlayer(player, Stage3Constants.CHARGE_DAMAGE, false, false, false);
                }
            }
            if (horizontalCollision) {
                level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.4F, 0.8F);
                entityData.set(ATTACK_TICK, Stage3Constants.CHARGE_WINDUP + Stage3Constants.CHARGE_ACTIVE);
            }
        } else {
            setDeltaMovement(getDeltaMovement().multiply(0.35D, 1.0D, 0.35D));
        }
    }

    public void finishAttack() {
        entityData.set(ATTACK_TYPE, AzureDragonAttackType.NONE.ordinal());
        entityData.set(ATTACK_TICK, 0);
        setNoGravity(false);
        setGlowingTag(false);
        setXRot(0.0F);
        slamImpacted = false;
        attackVictims.clear();
        attackCooldown = Stage3Constants.ATTACK_INTERVAL_MIN_TICKS
                + random.nextInt(Stage3Constants.ATTACK_INTERVAL_MAX_TICKS
                - Stage3Constants.ATTACK_INTERVAL_MIN_TICKS + 1);
    }

    private int windup(AzureDragonAttackType type) {
        return switch (type) {
            case PHYSICAL_BREATH -> Stage3Constants.PHYSICAL_BREATH_WINDUP;
            case STOMP -> Stage3Constants.STOMP_WINDUP;
            case TURNING_TAIL -> Stage3Constants.TURNING_TAIL_WINDUP;
            case AZURE_BREATH -> Stage3Constants.AZURE_BREATH_WINDUP;
            case DIVINE_TAIL -> Stage3Constants.DIVINE_TAIL_WINDUP;
            case BULL_AZURE_SLASH -> Stage3Constants.BULL_SLASH_WINDUP;
            case AERIAL_SLAM -> Stage3Constants.AERIAL_SLAM_WINDUP;
            case CHARGE -> Stage3Constants.CHARGE_WINDUP;
            case BITE -> Stage3Constants.BITE_WINDUP;
            default -> 0;
        };
    }

    private int totalDuration(AzureDragonAttackType type) {
        return switch (type) {
            case PHYSICAL_BREATH -> Stage3Constants.PHYSICAL_BREATH_WINDUP + Stage3Constants.PHYSICAL_BREATH_ACTIVE + Stage3Constants.PHYSICAL_BREATH_RECOVERY;
            case STOMP -> Stage3Constants.STOMP_WINDUP + Stage3Constants.STOMP_RECOVERY;
            case TURNING_TAIL -> Stage3Constants.TURNING_TAIL_WINDUP + Stage3Constants.TURNING_TAIL_RECOVERY;
            case AZURE_BREATH -> Stage3Constants.AZURE_BREATH_WINDUP + Stage3Constants.AZURE_BREATH_ACTIVE + Stage3Constants.AZURE_BREATH_RECOVERY;
            case DIVINE_TAIL -> Stage3Constants.DIVINE_TAIL_WINDUP + Stage3Constants.DIVINE_TAIL_RECOVERY;
            case BULL_AZURE_SLASH -> Stage3Constants.BULL_SLASH_WINDUP + Stage3Constants.BULL_SLASH_CHASE + Stage3Constants.BULL_SLASH_RECOVERY;
            case AERIAL_SLAM -> Stage3Constants.AERIAL_SLAM_WINDUP + Stage3Constants.AERIAL_SLAM_ACTIVE + Stage3Constants.AERIAL_SLAM_RECOVERY;
            case CHARGE -> Stage3Constants.CHARGE_WINDUP + Stage3Constants.CHARGE_ACTIVE + Stage3Constants.CHARGE_RECOVERY;
            case BITE -> Stage3Constants.BITE_WINDUP + Stage3Constants.BITE_RECOVERY;
            default -> 1;
        };
    }

    private void spawnWarning(AzureDragonAttackType type, int tick) {
        if (!(level() instanceof ServerLevel serverLevel) || tick % 2 != 0) return;
        if (type == AzureDragonAttackType.STOMP) {
            double radius = Stage3Constants.STOMP_RADIUS;
            for (int angle = 0; angle < 360; angle += 12) {
                double radians = Math.toRadians(angle);
                serverLevel.sendParticles(WARNING_DUST, getX() + Math.cos(radians) * radius, getY() + 0.1D,
                        getZ() + Math.sin(radians) * radius, 1, 0.02D, 0.02D, 0.02D, 0.0D);
            }
        } else if (type == AzureDragonAttackType.TURNING_TAIL || type == AzureDragonAttackType.DIVINE_TAIL) {
            double radius = type == AzureDragonAttackType.DIVINE_TAIL ? Stage3Constants.DIVINE_TAIL_RANGE : Stage3Constants.TURNING_TAIL_RANGE;
            for (int angle = 105; angle <= 255; angle += 8) {
                double radians = Math.toRadians(lockedAttackYaw + angle);
                serverLevel.sendParticles(WARNING_DUST, getX() - Math.sin(radians) * radius, getY() + 0.3D,
                        getZ() + Math.cos(radians) * radius, 1, 0.02D, 0.02D, 0.02D, 0.0D);
            }
        } else if (type == AzureDragonAttackType.BITE) {
            Vec3 direction = directionFromYaw(lockedAttackYaw);
            Vec3 mouth = position().add(0.0D, 2.2D, 0.0D).add(direction.scale(5.5D));
            serverLevel.sendParticles(WARNING_DUST, mouth.x, mouth.y, mouth.z,
                    12, 0.75D, 0.55D, 0.75D, 0.02D);
        } else if (type == AzureDragonAttackType.BULL_AZURE_SLASH) {
            Vec3 direction = directionFromYaw(lockedAttackYaw);
            Vec3 side = new Vec3(direction.z, 0.0D, -direction.x);
            Vec3 left = position().add(side.scale(4.5D)).add(0.0D, 2.0D, 0.0D);
            Vec3 right = position().add(side.scale(-4.5D)).add(0.0D, 2.0D, 0.0D);
            serverLevel.sendParticles(WARNING_DUST, left.x, left.y, left.z, 10,
                    0.8D, 0.45D, 0.8D, 0.02D);
            serverLevel.sendParticles(WARNING_DUST, right.x, right.y, right.z, 10,
                    0.8D, 0.45D, 0.8D, 0.02D);
        } else if (type == AzureDragonAttackType.AERIAL_SLAM) {
            drawWarningRing(serverLevel, lockedTargetPosition, Stage3Constants.AERIAL_SLAM_RADIUS);
            serverLevel.sendParticles(WARNING_DUST, lockedTargetPosition.x, lockedTargetPosition.y + 0.15D,
                    lockedTargetPosition.z, 8, 0.35D, 0.03D, 0.35D, 0.0D);
        } else if (type == AzureDragonAttackType.CHARGE) {
            Vec3 direction = directionFromYaw(lockedAttackYaw);
            Vec3 side = new Vec3(direction.z, 0.0D, -direction.x);
            for (int step = 2; step <= (int) Stage3Constants.CHARGE_RANGE; step += 2) {
                Vec3 center = position().add(direction.scale(step));
                for (double lateral : new double[]{-2.2D, 0.0D, 2.2D}) {
                    Vec3 marker = center.add(side.scale(lateral));
                    serverLevel.sendParticles(WARNING_DUST, marker.x, getY() + 0.18D, marker.z,
                            1, 0.01D, 0.01D, 0.01D, 0.0D);
                }
            }
        } else {
            Vec3 mouth = breathMouthPosition();
            serverLevel.sendParticles(WARNING_DUST, mouth.x, mouth.y, mouth.z,
                    10, 0.5D, 0.35D, 0.5D, 0.02D);
            Vec3 direction = directionFromYaw(lockedAttackYaw);
            int warningRange = type == AzureDragonAttackType.AZURE_BREATH
                    ? (int) Stage3Constants.AZURE_BREATH_RANGE : 15;
            if (type == AzureDragonAttackType.AZURE_BREATH) {
                double charge = Math.min(1.0D, tick / (double) Stage3Constants.AZURE_BREATH_WINDUP);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, mouth.x, mouth.y, mouth.z,
                        12 + (int) (charge * 18.0D), 0.35D + charge * 0.75D,
                        0.25D + charge * 0.55D, 0.35D + charge * 0.75D, 0.025D);
                serverLevel.sendParticles(ParticleTypes.END_ROD, mouth.x, mouth.y, mouth.z,
                        5, 0.45D, 0.35D, 0.45D, 0.015D);
            }
            for (int step = 3; step <= warningRange; step += 3) {
                Vec3 marker = position().add(direction.scale(step));
                serverLevel.sendParticles(type == AzureDragonAttackType.AZURE_BREATH
                                ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.CLOUD,
                        marker.x, getY() + 0.35D, marker.z, 2, 0.2D, 0.05D, 0.2D, 0.0D);
            }
        }
    }

    private void announceAzureBreathReleaseWarning() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        serverLevel.playSound(null, blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE,
                SoundSource.HOSTILE, 1.6F, 1.25F);
        for (ServerPlayer player : serverLevel.players()) {
            if (player != getTarget() && distanceToSqr(player) > Stage3Constants.AZURE_DRAGON_FOLLOW_RANGE
                    * Stage3Constants.AZURE_DRAGON_FOLLOW_RANGE) continue;
            player.displayClientMessage(Component.translatable(
                    "message.sevenstars.azure_dragon.telegraph.azure_breath_release"), true);
        }
    }

    private static void drawWarningRing(ServerLevel level, Vec3 center, double radius) {
        for (int angle = 0; angle < 360; angle += 10) {
            double radians = Math.toRadians(angle);
            level.sendParticles(WARNING_DUST, center.x + Math.cos(radians) * radius, center.y + 0.15D,
                    center.z + Math.sin(radians) * radius, 1, 0.01D, 0.01D, 0.01D, 0.0D);
        }
    }

    private void hitCone(boolean magic, double range, double halfAngle, float damage, boolean disableTriangle) {
        Vec3 forward = directionFromYaw(lockedAttackYaw);
        double minDot = Math.cos(Math.toRadians(halfAngle));
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(range),
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator())) {
            Vec3 delta = player.position().subtract(position());
            if (delta.lengthSqr() > range * range || Math.abs(delta.y) > 5.0D) continue;
            Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
            if (horizontal.lengthSqr() < 0.01D || forward.dot(horizontal.normalize()) < minDot || !hasLineOfSight(player)) continue;
            hurtPlayer(player, damage, magic, disableTriangle, false);
        }
    }

    private void hitBreathCone(boolean magic, double range, double halfAngle, float damage, boolean disableTriangle) {
        Vec3 origin = breathMouthPosition();
        Vec3 forward = directionFromRotation(lockedAttackYaw, lockedAttackPitch);
        double minDot = Math.cos(Math.toRadians(halfAngle));
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(range + 7.0D),
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator())) {
            Vec3 delta = player.getBoundingBox().getCenter().subtract(origin);
            if (delta.lengthSqr() > range * range || delta.lengthSqr() < 0.01D
                    || forward.dot(delta.normalize()) < minDot || !hasLineOfSight(player)) continue;
            hurtPlayer(player, damage, magic, disableTriangle, false);
        }
    }

    private void hitTail(double range, float damage, boolean disableTriangle) {
        Vec3 back = directionFromYaw(lockedAttackYaw).scale(-1.0D);
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(range), Player::isAlive)) {
            Vec3 delta = player.position().subtract(position());
            Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
            if (horizontal.lengthSqr() <= range * range && horizontal.lengthSqr() > 0.01D
                    && back.dot(horizontal.normalize()) >= -0.2D) {
                hurtPlayer(player, damage, false, disableTriangle, false);
            }
        }
    }

    private void hitRadius(double radius, float damage, boolean magic, boolean disableArmor) {
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(radius, 3.5D, radius),
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator())) {
            if (distanceToSqr(player) <= radius * radius) hurtPlayer(player, damage, magic, false, disableArmor);
        }
    }

    private void hurtPlayer(Player player, float damage, boolean magic, boolean disableTriangle, boolean disableArmor) {
        DamageSource source = magic ? damageSources().magic() : damageSources().mobAttack(this);
        if (!player.hurt(source, damage)) return;
        Vec3 push = player.position().subtract(position()).normalize().scale(0.65D);
        player.push(push.x, 0.18D, push.z);
        if (player instanceof ServerPlayer serverPlayer) {
            if (disableTriangle) SkillDisableManager.disableSkill(serverPlayer, SkillIds.TRIANGLE,
                    Stage3Constants.AZURE_TRIANGLE_DISABLE_TICKS);
            if (disableArmor) ArmorDisableManager.disableArmor(serverPlayer,
                    Stage3Constants.BULL_SLASH_ARMOR_DISABLE_TICKS);
        }
    }

    private static Vec3 directionFromYaw(float yaw) {
        double radians = Math.toRadians(yaw);
        return new Vec3(-Math.sin(radians), 0.0D, Math.cos(radians)).normalize();
    }

    private static Vec3 directionFromRotation(float yaw, float pitch) {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        double horizontal = Math.cos(pitchRadians);
        return new Vec3(-Math.sin(yawRadians) * horizontal, -Math.sin(pitchRadians),
                Math.cos(yawRadians) * horizontal).normalize();
    }

    private Vec3 breathMouthPosition() {
        return position().add(0.0D, 2.4D, 0.0D).add(directionFromYaw(lockedAttackYaw).scale(4.5D));
    }

    private void faceAttackTarget(LivingEntity target, float maxTurn, boolean lockDirectlyToTarget) {
        Vec3 delta = target.position().subtract(position());
        if (delta.horizontalDistanceSqr() < 0.0001D) return;
        float desiredYaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
        float facingYaw = Mth.approachDegrees(getYRot(), desiredYaw, maxTurn);
        setYRot(facingYaw);
        yBodyRot = facingYaw;
        setYHeadRot(facingYaw);
        lockedAttackYaw = lockDirectlyToTarget ? desiredYaw : facingYaw;
    }

    private void faceBreathPitch(LivingEntity target, float maxTurn) {
        Vec3 delta = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D)
                .subtract(breathMouthPosition());
        float desiredPitch = (float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance()));
        lockedAttackPitch = Mth.approachDegrees(lockedAttackPitch, desiredPitch, maxTurn);
        setXRot(lockedAttackPitch);
    }

    public boolean hurt(AzureDragonPart part, DamageSource source, float amount) {
        float multiplier;
        if (part == head) multiplier = 1.15F;
        else if (part == neck) multiplier = 0.85F;
        else if (part == body) multiplier = 0.75F;
        else if (part == leftWing || part == rightWing) multiplier = 0.55F;
        else multiplier = 0.45F;
        return hurt(source, amount * multiplier);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || isTransitioning() || isDefeated()) return false;
        float threshold = getPhase() == 1 ? getMaxHealth() * 2.0F / 3.0F
                : getPhase() == 2 ? getMaxHealth() / 3.0F : 0.0F;
        if (getPhase() < 3 && getHealth() - amount <= threshold) {
            float allowed = Math.max(0.0F, getHealth() - threshold);
            boolean result = allowed <= 0.0F || super.hurt(source, allowed);
            setHealth(threshold);
            beginPhaseTransition(getPhase() + 1);
            if (result) recordParticipant(source);
            return result;
        }
        if (getPhase() == 3 && amount >= getHealth()) {
            setHealth(1.0F);
            recordParticipant(source);
            beginDefeated();
            return true;
        }
        boolean result = super.hurt(source, amount);
        if (result) recordParticipant(source);
        return result;
    }

    private void recordParticipant(DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker instanceof ServerPlayer player) participants.add(player.getUUID());
        else if (source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile projectile
                && projectile.getOwner() instanceof ServerPlayer player) participants.add(player.getUUID());
    }

    private void beginDefeated() {
        finishAttack();
        entityData.set(DEFEATED, true);
        entityData.set(ATTACK_TYPE, AzureDragonAttackType.DEFEATED.ordinal());
        entityData.set(ATTACK_TICK, 0);
        setInvulnerable(true);
        setNoAi(true);
        getNavigation().stop();
        cleanupSummonedButchers();
        clearIllusions();
        level().playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 2.0F, 0.8F);
    }

    private void tickDefeated() {
        defeatTicks++;
        entityData.set(ATTACK_TICK, defeatTicks);
        if (level() instanceof ServerLevel serverLevel && defeatTicks % 20 == 0) {
            int star = Math.min(7, defeatTicks / 20);
            double angle = star * Math.PI * 2.0D / 7.0D;
            serverLevel.sendParticles(ParticleTypes.END_ROD, getX() + Math.cos(angle) * 2.8D,
                    getY() + 2.2D, getZ() + Math.sin(angle) * 2.8D, 18,
                    0.35D, 0.35D, 0.35D, 0.04D);
            serverLevel.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE,
                    1.2F, 0.7F + star * 0.06F);
            List<BlockPos> seals = findSealPositions(serverLevel);
            if (star <= seals.size()) {
                BlockPos seal = seals.get(star - 1);
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, seal.getX() + 0.5D,
                        seal.getY() + 0.7D, seal.getZ() + 0.5D, 24, 0.6D, 0.8D, 0.6D, 0.1D);
            }
        }
        if (defeatTicks >= Stage3Constants.DEFEAT_SEQUENCE_TICKS) {
            if (level() instanceof ServerLevel serverLevel) spawnFinalSevenStarArray(serverLevel);
            rewardParticipants();
            discard();
        }
    }

    private List<BlockPos> findSealPositions(ServerLevel level) {
        List<BlockPos> positions = new ArrayList<>();
        int radius = Stage3Constants.SEAL_SCAN_RADIUS;
        for (BlockPos pos : BlockPos.betweenClosed(arenaCenter.offset(-radius, -12, -radius),
                arenaCenter.offset(radius, 12, radius))) {
            if (level.getBlockState(pos).is(ModBlocks.AZURE_SEAL_CHAIN.get())) positions.add(pos.immutable());
        }
        positions.sort(java.util.Comparator.comparingDouble(pos -> pos.distSqr(arenaCenter)));
        return positions;
    }

    private void spawnFinalSevenStarArray(ServerLevel level) {
        Vec3 center = Vec3.atCenterOf(arenaCenter).add(0.0D, 1.0D, 0.0D);
        Vec3[] points = new Vec3[7];
        for (int index = 0; index < points.length; index++) {
            double angle = -Math.PI / 2.0D + index * Math.PI * 2.0D / 7.0D;
            points[index] = center.add(Math.cos(angle) * 7.0D, 0.0D, Math.sin(angle) * 7.0D);
            level.sendParticles(ParticleTypes.END_ROD, points[index].x, points[index].y, points[index].z,
                    28, 0.35D, 0.35D, 0.35D, 0.05D);
        }
        for (int index = 0; index < points.length; index++) {
            Vec3 from = points[index];
            Vec3 to = points[(index + 3) % points.length];
            for (int step = 0; step <= 24; step++) {
                Vec3 point = from.lerp(to, step / 24.0D);
                level.sendParticles(AZURE_DUST, point.x, point.y, point.z, 1, 0.01D, 0.01D, 0.01D, 0.0D);
            }
        }
    }

    private void rewardParticipants() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        for (UUID uuid : participants) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (player == null) continue;
            CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
            if (persisted.getBoolean("sevenstars_azure_dragon_reward")) continue;
            persisted.putBoolean("sevenstars_azure_dragon_reward", true);
            ItemStack reward = new ItemStack(ModItems.LOST_STAR_MAGIC_TOKEN.get());
            if (!player.getInventory().add(reward)) player.drop(reward, false);
            player.displayClientMessage(Component.translatable("message.sevenstars.azure_dragon_defeated"), false);
        }
    }

    private void tickButcherSummoning() {
        if (getPhase() < 2 || isTransitioning() || isDefeated()) return;
        butcherSummonTimer++;
        if (butcherSummonTimer < Stage3Constants.BUTCHER_SUMMON_INTERVAL_TICKS) return;
        butcherSummonTimer = 0;
        if (!(level() instanceof ServerLevel serverLevel)) return;
        AABB arena = arenaBounds();
        if (!serverLevel.getEntitiesOfClass(GoatHunterButcherEntity.class, arena,
                butcher -> butcher.isAlive()).isEmpty()) return;
        List<BlockPos> runes = new ArrayList<>();
        int radius = (int) Stage3Constants.ARENA_RADIUS;
        for (BlockPos pos : BlockPos.betweenClosed(arenaCenter.offset(-radius, -8, -radius),
                arenaCenter.offset(radius, 8, radius))) {
            if (serverLevel.getBlockState(pos).is(ModBlocks.AZURE_BUTCHER_SPAWN_RUNE.get())) runes.add(pos.immutable());
        }
        if (runes.isEmpty()) return;
        BlockPos rune = runes.get(random.nextInt(runes.size()));
        GoatHunterButcherEntity butcher = new GoatHunterButcherEntity(ModEntities.GOAT_HUNTER_BUTCHER.get(), serverLevel);
        butcher.moveTo(rune.getX() + 0.5D, rune.getY() + 1.0D, rune.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        if (!serverLevel.noCollision(butcher) || !serverLevel.getBlockState(rune).isSolidRender(serverLevel, rune)) return;
        butcher.markSummonedByAzureDragon(getUUID(), arenaCenter);
        if (getTarget() instanceof Player player) butcher.setTarget(player);
        serverLevel.addFreshEntity(butcher);
    }

    public void cleanupSummonedButchers() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        for (GoatHunterButcherEntity butcher : serverLevel.getEntitiesOfClass(GoatHunterButcherEntity.class, arenaBounds(),
                butcher -> butcher.isSummonedByAzureDragon(getUUID()))) butcher.discard();
    }

    private void syncIllusionPlayers() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        boolean active = getPhase() >= 3 && !isDefeated();
        Set<UUID> now = new HashSet<>();
        if (active) {
            for (ServerPlayer player : serverLevel.players()) {
                if (!player.isAlive() || (player != getTarget() && distanceToSqr(player) >
                        Stage3Constants.AZURE_DRAGON_FOLLOW_RANGE * Stage3Constants.AZURE_DRAGON_FOLLOW_RANGE)) continue;
                now.add(player.getUUID());
                if (!illusionPlayers.contains(player.getUUID())) sendIllusion(player, true);
            }
        }
        for (UUID uuid : new HashSet<>(illusionPlayers)) {
            if (!now.contains(uuid)) {
                ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
                if (player != null) sendIllusion(player, false);
            }
        }
        illusionPlayers.clear();
        illusionPlayers.addAll(now);
    }

    private void sendIllusion(ServerPlayer player, boolean active) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new QinglongIllusionPacket(active, getId()));
    }

    private void clearIllusions() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        for (UUID uuid : illusionPlayers) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) sendIllusion(player, false);
        }
        illusionPlayers.clear();
    }

    private AABB arenaBounds() {
        return new AABB(arenaCenter).inflate(Stage3Constants.ARENA_RADIUS,
                Stage3Constants.ARENA_VERTICAL_RANGE, Stage3Constants.ARENA_RADIUS);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
        if (getPhase() >= 3 && !isDefeated()) sendIllusion(player, true);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
        sendIllusion(player, false);
        illusionPlayers.remove(player.getUUID());
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            cleanupSummonedButchers();
            clearIllusions();
        }
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", getPhase());
        tag.putInt("AttackType", getAttackType().ordinal());
        tag.putInt("AttackTick", getAttackTick());
        tag.putBoolean("Transitioning", isTransitioning());
        tag.putBoolean("Defeated", isDefeated());
        tag.putInt("PendingPhase", pendingPhase);
        tag.putInt("TransitionTicks", transitionTicks);
        tag.putInt("ButcherSummonTimer", butcherSummonTimer);
        tag.putInt("DefeatTicks", defeatTicks);
        tag.putFloat("LockedAttackYaw", lockedAttackYaw);
        tag.putFloat("LockedAttackPitch", lockedAttackPitch);
        tag.putDouble("LockedTargetX", lockedTargetPosition.x);
        tag.putDouble("LockedTargetY", lockedTargetPosition.y);
        tag.putDouble("LockedTargetZ", lockedTargetPosition.z);
        tag.putInt("LastAttack", lastAttack.ordinal());
        tag.putBoolean("SlamImpacted", slamImpacted);
        tag.putInt("ArenaX", arenaCenter.getX());
        tag.putInt("ArenaY", arenaCenter.getY());
        tag.putInt("ArenaZ", arenaCenter.getZ());
        ListTag list = new ListTag();
        participants.forEach(uuid -> list.add(StringTag.valueOf(uuid.toString())));
        tag.put("Participants", list);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(PHASE, Mth.clamp(tag.getInt("Phase"), 1, 3));
        entityData.set(ATTACK_TYPE, AzureDragonAttackType.byId(tag.getInt("AttackType")).ordinal());
        entityData.set(ATTACK_TICK, Math.max(0, tag.getInt("AttackTick")));
        entityData.set(TRANSITIONING, tag.getBoolean("Transitioning"));
        entityData.set(DEFEATED, tag.getBoolean("Defeated"));
        pendingPhase = Mth.clamp(tag.getInt("PendingPhase"), 0, 3);
        transitionTicks = Math.max(0, tag.getInt("TransitionTicks"));
        butcherSummonTimer = Math.max(0, tag.getInt("ButcherSummonTimer"));
        defeatTicks = Math.max(0, tag.getInt("DefeatTicks"));
        lockedAttackYaw = tag.getFloat("LockedAttackYaw");
        lockedAttackPitch = tag.getFloat("LockedAttackPitch");
        lockedTargetPosition = new Vec3(tag.getDouble("LockedTargetX"), tag.getDouble("LockedTargetY"),
                tag.getDouble("LockedTargetZ"));
        lastAttack = AzureDragonAttackType.byId(tag.getInt("LastAttack"));
        slamImpacted = tag.getBoolean("SlamImpacted");
        arenaCenter = new BlockPos(tag.getInt("ArenaX"), tag.getInt("ArenaY"), tag.getInt("ArenaZ"));
        participants.clear();
        for (Tag entry : tag.getList("Participants", Tag.TAG_STRING)) {
            try {
                participants.add(UUID.fromString(entry.getAsString()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (isTransitioning() || isDefeated()) setInvulnerable(true);
        if (isDefeated()) setNoAi(true);
        if (getAttackType() == AzureDragonAttackType.AERIAL_SLAM && !slamImpacted) setNoGravity(true);
        if (getAttackType() != AzureDragonAttackType.NONE && !isDefeated()) setGlowingTag(true);
    }
}
