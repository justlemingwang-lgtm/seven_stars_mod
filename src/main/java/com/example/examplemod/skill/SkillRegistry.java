package com.example.examplemod.skill;

import com.example.examplemod.skill.impl.ChopSkill;
import com.example.examplemod.skill.impl.GoldenFingerSkill;
import com.example.examplemod.skill.impl.GoatHornSkill;
import com.example.examplemod.skill.impl.HoundClawSkill;
import com.example.examplemod.skill.impl.IceDipperShotSkill;
import com.example.examplemod.skill.impl.LightWaveSkill;
import com.example.examplemod.skill.impl.PegasusStepSkill;
import com.example.examplemod.skill.impl.SevenStarSkill;
import com.example.examplemod.skill.impl.SixMeridianSwordSkill;
import com.example.examplemod.skill.impl.TriangleSkill;
import com.example.examplemod.skill.impl.UnimplementedSkill;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class SkillRegistry {
    public static final String LIGHT_WAVE = "light_wave";
    public static final String SEVEN_STAR = "seven_star";
    public static final String SIX_MERIDIAN_SWORD = "six_meridian_sword";
    public static final String GOLDEN_FINGER = "golden_finger";
    public static final String PEGASUS_STEP = "pegasus_step";
    public static final String PEGASUS_LEAP = "pegasus_leap";
    public static final String PEGASUS_STARFALL = "pegasus_starfall";
    public static final String ICE_DIPPER_SHOT = "ice_dipper_shot";
    public static final String ICE_DIPPER_CHAIN = "ice_dipper_chain";
    public static final String FROZEN_DIPPER_SEAL = "frozen_dipper_seal";
    public static final String HOUND_CLAW = "hound_claw";
    public static final String HOUND_COMBO_CLAW = "hound_combo_claw";
    public static final String HOUND_PACK_REND = "hound_pack_rend";
    public static final String GOAT_HORN = "goat_horn";
    public static final String CHOP = "chop";
    public static final String TRIANGLE = "triangle";
    public static final String SEVEN_SCATTERED_STRIKES = "seven_scattered_strikes";

    private static final Map<String, Skill> SKILLS = new LinkedHashMap<>();

    static {
        register(new LightWaveSkill());
        register(new SevenStarSkill());
        register(new SixMeridianSwordSkill());
        register(new GoldenFingerSkill());
        register(new PegasusStepSkill());
        register(new UnimplementedSkill(PEGASUS_LEAP, SkillSeries.PEGASUS, SkillRank.MEDIUM));
        register(new UnimplementedSkill(PEGASUS_STARFALL, SkillSeries.PEGASUS, SkillRank.ULTIMATE));
        register(new IceDipperShotSkill());
        register(new UnimplementedSkill(ICE_DIPPER_CHAIN, SkillSeries.ICE_DIPPER, SkillRank.MEDIUM));
        register(new UnimplementedSkill(FROZEN_DIPPER_SEAL, SkillSeries.ICE_DIPPER, SkillRank.ULTIMATE));
        register(new HoundClawSkill());
        register(new UnimplementedSkill(HOUND_COMBO_CLAW, SkillSeries.HOUND_CLAW, SkillRank.MEDIUM));
        register(new UnimplementedSkill(HOUND_PACK_REND, SkillSeries.HOUND_CLAW, SkillRank.ULTIMATE));
        register(new GoatHornSkill());
        register(new ChopSkill());
        register(new TriangleSkill());
    }

    private static void register(Skill skill) {
        SKILLS.put(skill.id(), skill);
    }

    public static Optional<Skill> get(String skillId) {
        return Optional.ofNullable(SKILLS.get(skillId));
    }

    public static Collection<Skill> all() {
        return SKILLS.values();
    }

    public static Optional<Skill> getBySeriesRank(SkillSeries series, SkillRank rank) {
        return SKILLS.values().stream()
                .filter(skill -> skill.series() == series && skill.rank() == rank)
                .findFirst();
    }

    public static Stream<Skill> skillsForSeries(SkillSeries series) {
        return SKILLS.values().stream().filter(skill -> skill.series() == series);
    }
}
