package com.example.examplemod.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class BloodyCleaverItem extends SwordItem {
    public BloodyCleaverItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }
}
