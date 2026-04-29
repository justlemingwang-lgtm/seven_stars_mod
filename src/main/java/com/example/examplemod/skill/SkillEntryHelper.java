package com.example.examplemod.skill;

import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Optional;

public class SkillEntryHelper {
    private static final String SERIES_PREFIX = "series:";

    public static String seriesEntry(SkillSeries series) {
        return SERIES_PREFIX + series.name().toLowerCase(Locale.ROOT);
    }

    public static Optional<SkillSeries> parseSeriesEntry(String entry) {
        if (entry == null || !entry.startsWith(SERIES_PREFIX)) {
            return Optional.empty();
        }
        String id = entry.substring(SERIES_PREFIX.length()).toUpperCase(Locale.ROOT);
        try {
            return Optional.of(SkillSeries.valueOf(id));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public static boolean isSeriesEntry(String entry) {
        return parseSeriesEntry(entry).isPresent();
    }

    public static Component displayName(String entry) {
        Optional<SkillSeries> series = parseSeriesEntry(entry);
        if (series.isPresent()) {
            return seriesDisplayName(series.get());
        }
        return SkillRegistry.get(entry).map(Skill::displayName).orElse(Component.literal(entry));
    }

    public static Component seriesDisplayName(SkillSeries series) {
        return switch (series) {
            case PEGASUS -> Component.translatable("series.sevenstars.pegasus");
            case ICE_DIPPER -> Component.translatable("series.sevenstars.ice_dipper");
            case HOUND_CLAW -> Component.translatable("series.sevenstars.hound_claw");
            case BASIC -> Component.translatable("series.sevenstars.basic");
            case DEFENSE -> Component.translatable("series.sevenstars.defense");
            case UTILITY -> Component.translatable("series.sevenstars.utility");
        };
    }
}
