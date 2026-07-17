package com.example.examplemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class AltarCultistEntity extends AncientCultistEntity {
    public AltarCultistEntity(EntityType<? extends AltarCultistEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 48.0D).add(Attributes.ARMOR, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 28.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }

    @Override protected float rangedDamage() { return 8.0F; }
    @Override protected int rangedDelay() { return 120; }
}
