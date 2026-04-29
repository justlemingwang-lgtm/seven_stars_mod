package com.example.examplemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    private static final ForgeConfigSpec.DoubleValue MAX_QI_DEFAULT = BUILDER
            .comment("Default maximum Qi for newly attached living entities.")
            .defineInRange("qi.maxQiDefault", 100.0D, 1.0D, Double.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue QI_REGEN_PERCENT_PER_SECOND = BUILDER
            .comment("Percent of max Qi regenerated every second. 0.02 means 2%.")
            .defineInRange("qi.qiRegenPercentPerSecond", 0.02D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue QI_REGEN_BASE_PERCENT_PER_SECOND = BUILDER
            .comment("Base percent of max Qi regenerated every second.")
            .defineInRange("qi.qiRegenBasePercentPerSecond", 0.02D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue QI_SURGE_ONE_PERCENT_PER_SECOND = BUILDER
            .comment("Percent of max Qi regenerated every second with Qi Surge I.")
            .defineInRange("qi.qiSurgeOnePercentPerSecond", 0.03D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue QI_SURGE_TWO_PERCENT_PER_SECOND = BUILDER
            .comment("Percent of max Qi regenerated every second with Qi Surge II.")
            .defineInRange("qi.qiSurgeTwoPercentPerSecond", 0.04D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue QI_EXHAUSTION_PERCENT_PER_SECOND = BUILDER
            .comment("Percent of max Qi regenerated every second with Qi Exhaustion.")
            .defineInRange("qi.qiExhaustionPercentPerSecond", 0.01D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue TOOL_DRAIN_AMOUNT = BUILDER
            .comment("Qi drained from a target by the absorber.")
            .defineInRange("qi.toolDrainAmount", 10.0D, 0.0D, Double.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue TOOL_CONSUME_AMOUNT = BUILDER
            .comment("Qi consumed from the user by the consumer.")
            .defineInRange("qi.toolConsumeAmount", 10.0D, 0.0D, Double.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue TOOL_ADD_AMOUNT = BUILDER
            .comment("Reserved amount for future Qi-adding test tools.")
            .defineInRange("qi.toolAddAmount", 10.0D, 0.0D, Double.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue SEVEN_STAR_SCROLL_BASE_SLOTS = BUILDER
            .comment("Base skill slots on a Seven Star Scroll.")
            .defineInRange("skill.sevenStarScrollBaseSlots", 5, 1, 9);

    private static final ForgeConfigSpec.ConfigValue<String> SKILL_WHEEL_KEY_DEFAULT = BUILDER
            .comment("Documented default key for opening the skill wheel. Actual key mapping is registered client-side.")
            .define("skill.skillWheelKeyDefault", "Z");

    private static final ForgeConfigSpec.ConfigValue<String> CAST_SELECTED_SKILL_KEY_DEFAULT = BUILDER
            .comment("Documented default key for casting the selected skill. Actual key mapping is registered client-side.")
            .define("skill.castSelectedSkillKeyDefault", "R");

    private static final ForgeConfigSpec.BooleanValue ENABLE_NEGATIVE_QI_PENALTY = BUILDER
            .comment("Whether skills trigger a backlash when they drive target Qi below zero.")
            .define("skill.enableNegativeQiPenalty", true);

    private static final ForgeConfigSpec.DoubleValue NEGATIVE_QI_HEALTH_PERCENT_DAMAGE = BUILDER
            .comment("Percent of target max health dealt when negative Qi backlash triggers.")
            .defineInRange("skill.negativeQiHealthPercentDamage", 0.33D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue NEGATIVE_QI_PROTECTION_RECOVER_PERCENT = BUILDER
            .comment("Percent of max Qi restored after negative Qi backlash triggers.")
            .defineInRange("skill.negativeQiProtectionRecoverPercent", 0.33D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.BooleanValue TOOLS_TRIGGER_NEGATIVE_QI_PENALTY = BUILDER
            .comment("Whether test tools can trigger negative Qi backlash.")
            .define("skill.toolsTriggerNegativeQiPenalty", false);

    private static final ForgeConfigSpec.IntValue PLAYER_HURT_QI_LOSS_MIN = BUILDER
            .comment("Minimum Qi lost when a player actually takes damage.")
            .defineInRange("combat.playerHurtQiLossMin", 5, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue PLAYER_HURT_QI_LOSS_MAX = BUILDER
            .comment("Maximum Qi lost when a player actually takes damage.")
            .defineInRange("combat.playerHurtQiLossMax", 10, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue PLAYER_HURT_QI_LOSS_CAN_TRIGGER_COLLAPSE = BUILDER
            .comment("Whether player hurt Qi loss can trigger negative Qi collapse.")
            .define("combat.playerHurtQiLossCanTriggerCollapse", true);

    private static final ForgeConfigSpec.DoubleValue CRACKED_CLAW_COMBO_DAMAGE_BONUS_PER_STACK = BUILDER
            .comment("Damage bonus per Cracked Claw Combo stack.")
            .defineInRange("item.crackedClawComboDamageBonusPerStack", 0.20D, 0.0D, 10.0D);

    private static final ForgeConfigSpec.IntValue CRACKED_CLAW_COMBO_MAX_STACKS = BUILDER
            .comment("Maximum Cracked Claw Combo stacks.")
            .defineInRange("item.crackedClawComboMaxStacks", 5, 1, 32);

    private static final ForgeConfigSpec.IntValue CRACKED_CLAW_COMBO_DURATION_TICKS = BUILDER
            .comment("Duration refreshed by each successful Cracked Claw Dagger hit.")
            .defineInRange("item.crackedClawComboDurationTicks", 120, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue QI_EXHAUSTION_FROM_ICE_TICKS = BUILDER
            .comment("Qi Exhaustion duration applied by Ice Dipper and Frost Marrow hits.")
            .defineInRange("combat.qiExhaustionFromIceTicks", 120, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue ENABLE_QI_COLLAPSE_PROTECTION = BUILDER
            .comment("Reserved switch for a future player Qi collapse protection window.")
            .define("combat.enableQiCollapseProtection", false);

    private static final ForgeConfigSpec.IntValue QI_COLLAPSE_PROTECTION_TICKS = BUILDER
            .comment("Reserved duration for a future player Qi collapse protection window.")
            .defineInRange("combat.qiCollapseProtectionTicks", 0, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue LIGHT_WAVE_QI_COST = BUILDER.defineInRange("skill.lightWaveQiCost", 20, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue LIGHT_WAVE_DAMAGE = BUILDER.defineInRange("skill.lightWaveDamage", 4.0D, 0.0D, Double.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue LIGHT_WAVE_COOLDOWN_TICKS = BUILDER.defineInRange("skill.lightWaveCooldownTicks", 20, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue LIGHT_WAVE_RANGE = BUILDER.defineInRange("skill.lightWaveRange", 12.0D, 1.0D, 128.0D);

    private static final ForgeConfigSpec.IntValue SEVEN_STAR_QI_COST = BUILDER.defineInRange("skill.sevenStarQiCost", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue SEVEN_STAR_COOLDOWN_TICKS = BUILDER.defineInRange("skill.sevenStarCooldownTicks", 200, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue SEVEN_STAR_CAST_TICKS = BUILDER.defineInRange("skill.sevenStarCastTicks", 40, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue SEVEN_STAR_INTERRUPT_WINDOW_TICKS = BUILDER.defineInRange("skill.sevenStarInterruptWindowTicks", 30, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue SEVEN_STAR_RECOVER_PERCENT = BUILDER.defineInRange("skill.sevenStarRecoverPercent", 0.20D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.IntValue SIX_MERIDIAN_SWORD_QI_COST = BUILDER.defineInRange("skill.sixMeridianSwordQiCost", 40, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue SIX_MERIDIAN_SWORD_DAMAGE = BUILDER.defineInRange("skill.sixMeridianSwordDamage", 7.0D, 0.0D, Double.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue SIX_MERIDIAN_SWORD_DRAIN_QI = BUILDER.defineInRange("skill.sixMeridianSwordDrainQi", 20, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue SIX_MERIDIAN_SWORD_COOLDOWN_TICKS = BUILDER.defineInRange("skill.sixMeridianSwordCooldownTicks", 13, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue SIX_MERIDIAN_SWORD_RANGE = BUILDER.defineInRange("skill.sixMeridianSwordRange", 3.0D, 1.0D, 16.0D);
    private static final ForgeConfigSpec.DoubleValue SIX_MERIDIAN_SWORD_SELF_RECOVER_RATIO = BUILDER.defineInRange("skill.sixMeridianSwordSelfRecoverRatio", 0.5D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.IntValue GOLDEN_FINGER_QI_COST = BUILDER.defineInRange("skill.goldenFingerQiCost", 60, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue GOLDEN_FINGER_DAMAGE = BUILDER.defineInRange("skill.goldenFingerDamage", 10.0D, 0.0D, Double.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue GOLDEN_FINGER_DRAIN_QI = BUILDER.defineInRange("skill.goldenFingerDrainQi", 40, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue GOLDEN_FINGER_COOLDOWN_TICKS = BUILDER.defineInRange("skill.goldenFingerCooldownTicks", 120, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue GOLDEN_FINGER_RANGE = BUILDER.defineInRange("skill.goldenFingerRange", 16.0D, 1.0D, 128.0D);

    private static final ForgeConfigSpec.IntValue PEGASUS_STEP_QI_COST = BUILDER.defineInRange("skill.pegasusStepQiCost", 20, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue PEGASUS_STEP_COOLDOWN_TICKS = BUILDER.defineInRange("skill.pegasusStepCooldownTicks", 13, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue PEGASUS_STEP_DISTANCE = BUILDER.defineInRange("skill.pegasusStepDistance", 8.0D, 1.0D, 32.0D);
    private static final ForgeConfigSpec.IntValue PEGASUS_STEP_DURATION_TICKS = BUILDER.defineInRange("skill.pegasusStepDurationTicks", 8, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue PEGASUS_STEP_DAMAGE = BUILDER.defineInRange("skill.pegasusStepDamage", 5.0D, 0.0D, Double.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue PEGASUS_STEP_DRAIN_QI = BUILDER.defineInRange("skill.pegasusStepDrainQi", 10, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue PEGASUS_STEP_HIT_RECOVER_QI = BUILDER.defineInRange("skill.pegasusStepHitRecoverQi", 10, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue PEGASUS_STEP_FALL_PROTECTION_TICKS = BUILDER.defineInRange("skill.pegasusStepFallProtectionTicks", 40, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue PEGASUS_STEP_SPEED_BUFF_TICKS = BUILDER.defineInRange("skill.pegasusStepSpeedBuffTicks", 40, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ICE_DIPPER_QI_COST = BUILDER.defineInRange("skill.iceDipperQiCost", 18, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue ICE_DIPPER_COOLDOWN_TICKS = BUILDER.defineInRange("skill.iceDipperCooldownTicks", 24, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue ICE_DIPPER_MIN_DAMAGE = BUILDER.defineInRange("skill.iceDipperMinDamage", 3.0D, 0.0D, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue ICE_DIPPER_MAX_DAMAGE = BUILDER.defineInRange("skill.iceDipperMaxDamage", 9.0D, 0.0D, Double.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue ICE_DIPPER_CHARGE_TICKS = BUILDER.defineInRange("skill.iceDipperChargeTicks", 200, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue ICE_DIPPER_RANGE = BUILDER.defineInRange("skill.iceDipperRange", 14.0D, 1.0D, 128.0D);
    private static final ForgeConfigSpec.DoubleValue ICE_DIPPER_PROJECTILE_SPEED = BUILDER.defineInRange("skill.iceDipperProjectileSpeed", 0.9D, 0.1D, 8.0D);
    private static final ForgeConfigSpec.IntValue ICE_DIPPER_DRAIN_QI = BUILDER.defineInRange("skill.iceDipperDrainQi", 8, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue ICE_DIPPER_SLOW_TICKS = BUILDER.defineInRange("skill.iceDipperSlowTicks", 40, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue HOUND_CLAW_QI_COST = BUILDER.defineInRange("skill.houndClawQiCost", 35, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue HOUND_CLAW_COOLDOWN_TICKS = BUILDER.defineInRange("skill.houndClawCooldownTicks", 13, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue HOUND_CLAW_DAMAGE = BUILDER.defineInRange("skill.houndClawDamage", 6.0D, 0.0D, Double.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue HOUND_CLAW_DRAIN_QI = BUILDER.defineInRange("skill.houndClawDrainQi", 18, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue HOUND_CLAW_RANGE = BUILDER.defineInRange("skill.houndClawRange", 3.2D, 1.0D, 16.0D);
    private static final ForgeConfigSpec.DoubleValue HOUND_CLAW_ANGLE = BUILDER.defineInRange("skill.houndClawAngle", 70.0D, 1.0D, 180.0D);
    private static final ForgeConfigSpec.IntValue HOUND_MARK_TICKS = BUILDER.defineInRange("skill.houndMarkTicks", 120, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue HOUND_MARK_DAMAGE_BONUS = BUILDER.defineInRange("skill.houndMarkDamageBonus", 0.20D, 0.0D, 10.0D);
    private static final ForgeConfigSpec.DoubleValue HOUND_LOW_QI_THRESHOLD = BUILDER.defineInRange("skill.houndLowQiThreshold", 0.30D, 0.0D, 1.0D);
    private static final ForgeConfigSpec.DoubleValue HOUND_LOW_QI_BONUS_DAMAGE = BUILDER.defineInRange("skill.houndLowQiBonusDamage", 3.0D, 0.0D, Double.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue HOUND_LOW_QI_BONUS_DRAIN = BUILDER.defineInRange("skill.houndLowQiBonusDrain", 8, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue QI_HELMET_MAX_QI = BUILDER
            .comment("Max Qi while wearing the test Qi Helmet.")
            .defineInRange("skill.qiHelmetMaxQi", 10000, 1, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;
    public static double maxQiDefault = 100.0D;
    public static double qiRegenPercentPerSecond = 0.02D;
    public static double qiRegenBasePercentPerSecond = 0.02D;
    public static double qiSurgeOnePercentPerSecond = 0.03D;
    public static double qiSurgeTwoPercentPerSecond = 0.04D;
    public static double qiExhaustionPercentPerSecond = 0.01D;
    public static double toolDrainAmount = 10.0D;
    public static double toolConsumeAmount = 10.0D;
    public static double toolAddAmount = 10.0D;
    public static int sevenStarScrollBaseSlots = 5;
    public static String skillWheelKeyDefault = "Z";
    public static String castSelectedSkillKeyDefault = "R";
    public static boolean enableNegativeQiPenalty = true;
    public static double negativeQiHealthPercentDamage = 0.33D;
    public static double negativeQiProtectionRecoverPercent = 0.33D;
    public static boolean toolsTriggerNegativeQiPenalty = false;
    public static int playerHurtQiLossMin = 5;
    public static int playerHurtQiLossMax = 10;
    public static boolean playerHurtQiLossCanTriggerCollapse = true;
    public static double crackedClawComboDamageBonusPerStack = 0.20D;
    public static int crackedClawComboMaxStacks = 5;
    public static int crackedClawComboDurationTicks = 120;
    public static int qiExhaustionFromIceTicks = 120;
    public static boolean enableQiCollapseProtection = false;
    public static int qiCollapseProtectionTicks = 0;
    public static int lightWaveQiCost = 20;
    public static double lightWaveDamage = 4.0D;
    public static int lightWaveCooldownTicks = 20;
    public static double lightWaveRange = 12.0D;
    public static int sevenStarQiCost = 0;
    public static int sevenStarCooldownTicks = 200;
    public static int sevenStarCastTicks = 40;
    public static int sevenStarInterruptWindowTicks = 30;
    public static double sevenStarRecoverPercent = 0.20D;
    public static int sixMeridianSwordQiCost = 40;
    public static double sixMeridianSwordDamage = 7.0D;
    public static int sixMeridianSwordDrainQi = 20;
    public static int sixMeridianSwordCooldownTicks = 13;
    public static double sixMeridianSwordRange = 3.0D;
    public static double sixMeridianSwordSelfRecoverRatio = 0.5D;
    public static int goldenFingerQiCost = 60;
    public static double goldenFingerDamage = 10.0D;
    public static int goldenFingerDrainQi = 40;
    public static int goldenFingerCooldownTicks = 120;
    public static double goldenFingerRange = 16.0D;
    public static int pegasusStepQiCost = 20;
    public static int pegasusStepCooldownTicks = 13;
    public static double pegasusStepDistance = 8.0D;
    public static int pegasusStepDurationTicks = 8;
    public static double pegasusStepDamage = 5.0D;
    public static int pegasusStepDrainQi = 10;
    public static int pegasusStepHitRecoverQi = 10;
    public static int pegasusStepFallProtectionTicks = 40;
    public static int pegasusStepSpeedBuffTicks = 40;
    public static int iceDipperQiCost = 18;
    public static int iceDipperCooldownTicks = 24;
    public static double iceDipperMinDamage = 3.0D;
    public static double iceDipperMaxDamage = 9.0D;
    public static int iceDipperChargeTicks = 200;
    public static double iceDipperRange = 14.0D;
    public static double iceDipperProjectileSpeed = 0.9D;
    public static int iceDipperDrainQi = 8;
    public static int iceDipperSlowTicks = 40;
    public static int houndClawQiCost = 35;
    public static int houndClawCooldownTicks = 13;
    public static double houndClawDamage = 6.0D;
    public static int houndClawDrainQi = 18;
    public static double houndClawRange = 3.2D;
    public static double houndClawAngle = 70.0D;
    public static int houndMarkTicks = 120;
    public static double houndMarkDamageBonus = 0.20D;
    public static double houndLowQiThreshold = 0.30D;
    public static double houndLowQiBonusDamage = 3.0D;
    public static int houndLowQiBonusDrain = 8;
    public static int qiHelmetMaxQi = 10000;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());

        maxQiDefault = MAX_QI_DEFAULT.get();
        qiRegenPercentPerSecond = QI_REGEN_PERCENT_PER_SECOND.get();
        qiRegenBasePercentPerSecond = QI_REGEN_BASE_PERCENT_PER_SECOND.get();
        qiSurgeOnePercentPerSecond = QI_SURGE_ONE_PERCENT_PER_SECOND.get();
        qiSurgeTwoPercentPerSecond = QI_SURGE_TWO_PERCENT_PER_SECOND.get();
        qiExhaustionPercentPerSecond = QI_EXHAUSTION_PERCENT_PER_SECOND.get();
        toolDrainAmount = TOOL_DRAIN_AMOUNT.get();
        toolConsumeAmount = TOOL_CONSUME_AMOUNT.get();
        toolAddAmount = TOOL_ADD_AMOUNT.get();
        sevenStarScrollBaseSlots = SEVEN_STAR_SCROLL_BASE_SLOTS.get();
        skillWheelKeyDefault = SKILL_WHEEL_KEY_DEFAULT.get();
        castSelectedSkillKeyDefault = CAST_SELECTED_SKILL_KEY_DEFAULT.get();
        enableNegativeQiPenalty = ENABLE_NEGATIVE_QI_PENALTY.get();
        negativeQiHealthPercentDamage = NEGATIVE_QI_HEALTH_PERCENT_DAMAGE.get();
        negativeQiProtectionRecoverPercent = NEGATIVE_QI_PROTECTION_RECOVER_PERCENT.get();
        toolsTriggerNegativeQiPenalty = TOOLS_TRIGGER_NEGATIVE_QI_PENALTY.get();
        playerHurtQiLossMin = PLAYER_HURT_QI_LOSS_MIN.get();
        playerHurtQiLossMax = PLAYER_HURT_QI_LOSS_MAX.get();
        playerHurtQiLossCanTriggerCollapse = PLAYER_HURT_QI_LOSS_CAN_TRIGGER_COLLAPSE.get();
        crackedClawComboDamageBonusPerStack = CRACKED_CLAW_COMBO_DAMAGE_BONUS_PER_STACK.get();
        crackedClawComboMaxStacks = CRACKED_CLAW_COMBO_MAX_STACKS.get();
        crackedClawComboDurationTicks = CRACKED_CLAW_COMBO_DURATION_TICKS.get();
        qiExhaustionFromIceTicks = QI_EXHAUSTION_FROM_ICE_TICKS.get();
        enableQiCollapseProtection = ENABLE_QI_COLLAPSE_PROTECTION.get();
        qiCollapseProtectionTicks = QI_COLLAPSE_PROTECTION_TICKS.get();
        lightWaveQiCost = LIGHT_WAVE_QI_COST.get();
        lightWaveDamage = LIGHT_WAVE_DAMAGE.get();
        lightWaveCooldownTicks = LIGHT_WAVE_COOLDOWN_TICKS.get();
        lightWaveRange = LIGHT_WAVE_RANGE.get();
        sevenStarQiCost = SEVEN_STAR_QI_COST.get();
        sevenStarCooldownTicks = SEVEN_STAR_COOLDOWN_TICKS.get();
        sevenStarCastTicks = SEVEN_STAR_CAST_TICKS.get();
        sevenStarInterruptWindowTicks = SEVEN_STAR_INTERRUPT_WINDOW_TICKS.get();
        sevenStarRecoverPercent = SEVEN_STAR_RECOVER_PERCENT.get();
        sixMeridianSwordQiCost = SIX_MERIDIAN_SWORD_QI_COST.get();
        sixMeridianSwordDamage = SIX_MERIDIAN_SWORD_DAMAGE.get();
        sixMeridianSwordDrainQi = SIX_MERIDIAN_SWORD_DRAIN_QI.get();
        sixMeridianSwordCooldownTicks = SIX_MERIDIAN_SWORD_COOLDOWN_TICKS.get();
        sixMeridianSwordRange = SIX_MERIDIAN_SWORD_RANGE.get();
        sixMeridianSwordSelfRecoverRatio = SIX_MERIDIAN_SWORD_SELF_RECOVER_RATIO.get();
        goldenFingerQiCost = GOLDEN_FINGER_QI_COST.get();
        goldenFingerDamage = GOLDEN_FINGER_DAMAGE.get();
        goldenFingerDrainQi = GOLDEN_FINGER_DRAIN_QI.get();
        goldenFingerCooldownTicks = GOLDEN_FINGER_COOLDOWN_TICKS.get();
        goldenFingerRange = GOLDEN_FINGER_RANGE.get();
        pegasusStepQiCost = PEGASUS_STEP_QI_COST.get();
        pegasusStepCooldownTicks = PEGASUS_STEP_COOLDOWN_TICKS.get();
        pegasusStepDistance = PEGASUS_STEP_DISTANCE.get();
        pegasusStepDurationTicks = PEGASUS_STEP_DURATION_TICKS.get();
        pegasusStepDamage = PEGASUS_STEP_DAMAGE.get();
        pegasusStepDrainQi = PEGASUS_STEP_DRAIN_QI.get();
        pegasusStepHitRecoverQi = PEGASUS_STEP_HIT_RECOVER_QI.get();
        pegasusStepFallProtectionTicks = PEGASUS_STEP_FALL_PROTECTION_TICKS.get();
        pegasusStepSpeedBuffTicks = PEGASUS_STEP_SPEED_BUFF_TICKS.get();
        iceDipperQiCost = ICE_DIPPER_QI_COST.get();
        iceDipperCooldownTicks = ICE_DIPPER_COOLDOWN_TICKS.get();
        iceDipperMinDamage = ICE_DIPPER_MIN_DAMAGE.get();
        iceDipperMaxDamage = ICE_DIPPER_MAX_DAMAGE.get();
        iceDipperChargeTicks = ICE_DIPPER_CHARGE_TICKS.get();
        iceDipperRange = ICE_DIPPER_RANGE.get();
        iceDipperProjectileSpeed = ICE_DIPPER_PROJECTILE_SPEED.get();
        iceDipperDrainQi = ICE_DIPPER_DRAIN_QI.get();
        iceDipperSlowTicks = ICE_DIPPER_SLOW_TICKS.get();
        houndClawQiCost = HOUND_CLAW_QI_COST.get();
        houndClawCooldownTicks = HOUND_CLAW_COOLDOWN_TICKS.get();
        houndClawDamage = HOUND_CLAW_DAMAGE.get();
        houndClawDrainQi = HOUND_CLAW_DRAIN_QI.get();
        houndClawRange = HOUND_CLAW_RANGE.get();
        houndClawAngle = HOUND_CLAW_ANGLE.get();
        houndMarkTicks = HOUND_MARK_TICKS.get();
        houndMarkDamageBonus = HOUND_MARK_DAMAGE_BONUS.get();
        houndLowQiThreshold = HOUND_LOW_QI_THRESHOLD.get();
        houndLowQiBonusDamage = HOUND_LOW_QI_BONUS_DAMAGE.get();
        houndLowQiBonusDrain = HOUND_LOW_QI_BONUS_DRAIN.get();
        qiHelmetMaxQi = QI_HELMET_MAX_QI.get();
    }

    public static int getMaxQiDefaultInt() {
        return Math.max(1, (int) Math.round(maxQiDefault));
    }

    public static int getToolDrainAmountInt() {
        return Math.max(0, (int) Math.round(toolDrainAmount));
    }

    public static int getToolConsumeAmountInt() {
        return Math.max(0, (int) Math.round(toolConsumeAmount));
    }

    public static int getToolAddAmountInt() {
        return Math.max(0, (int) Math.round(toolAddAmount));
    }
}
