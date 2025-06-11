package me.contaria.seedqueue.interfaces;

import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;

import java.util.List;

public interface SQChunkGenerator {

    boolean seedqueue$hasStrongholds();

    ChunkPos[] seedqueue$getStartPositions();

    void seedqueue$setStartPositions(ChunkPos[] startPositions);

    List<StructureStart> seedqueue$getStarts();
}
