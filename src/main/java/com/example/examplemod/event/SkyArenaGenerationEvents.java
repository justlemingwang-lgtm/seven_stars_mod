package com.example.examplemod.event;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.registry.ModBlocks;
import com.example.examplemod.worldgen.structure.SkyArenaStructure;
import com.example.examplemod.worldgen.structure.SkyArenaStructurePiece;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Repairs located sky-arena starts whose templates were not written into their chunks. */
@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class SkyArenaGenerationEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<Long> REPAIRING_STARTS = ConcurrentHashMap.newKeySet();

    private SkyArenaGenerationEvents() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD
                || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(SkyArenaStructure.ID);
        if (structure == null) return;
        StructureStart start = chunk.getStartForStructure(structure);
        if (start == null || !start.isValid() || !start.getChunkPos().equals(chunk.getPos())) return;

        ChunkPos startChunk = start.getChunkPos();
        long repairKey = startChunk.toLong();
        if (!REPAIRING_STARTS.add(repairKey)) return;
        level.getServer().execute(() -> {
            try {
                ensureTemplatesPresent(level, start);
            } finally {
                REPAIRING_STARTS.remove(repairKey);
            }
        });
    }

    private static void ensureTemplatesPresent(ServerLevel level, StructureStart start) {
        BoundingBox bounds = start.getBoundingBox();
        BlockPos boundsCenter = bounds.getCenter();
        int floorY = bounds.minY() + SkyArenaStructure.FLOOR_LEVEL_IN_TEMPLATE;
        BlockPos centerAtFloor = new BlockPos(boundsCenter.getX(), floorY, boundsCenter.getZ());
        BlockPos containerPos = centerAtFloor.above();
        if (level.getBlockState(containerPos).is(ModBlocks.AZURE_SOUL_CONTAINER.get())) return;

        int originY = floorY - SkyArenaStructure.FLOOR_LEVEL_IN_TEMPLATE;
        int placed = 0;
        for (SkyArenaStructure.TemplatePlacement template : SkyArenaStructure.templates()) {
            BlockPos origin = new BlockPos(centerAtFloor.getX() + template.offsetX(), originY,
                    centerAtFloor.getZ() + template.offsetZ());
            if (SkyArenaStructurePiece.placeDirect(level, template.name(), origin)) placed++;
        }
        LOGGER.warn("Repaired empty sky arena start at {}: placed {}/9 templates", centerAtFloor, placed);
    }
}
