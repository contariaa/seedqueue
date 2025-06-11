package me.contaria.seedqueue.mixin.server.synchronization;

import me.contaria.seedqueue.interfaces.SQChunkGenerator;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin implements SQChunkGenerator {
    @Unique
    private final List<StructureStart> starts = new ArrayList<>();
    @Unique
    private ChunkPos[] startPositions;

    @Override
    public boolean seedqueue$hasStrongholds() {
        return this.startPositions != null;
    }

    @Override
    public ChunkPos[] seedqueue$getStartPositions() {
        return this.startPositions;
    }

    @Override
    public void seedqueue$setStartPositions(ChunkPos[] startPositions) {
        this.startPositions = startPositions;
    }

    @Override
    public List<StructureStart> seedqueue$getStarts() {
        return this.starts;
    }
}
