package me.contaria.seedqueue.mixin.server.synchronization;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.contaria.seedqueue.interfaces.SQChunkGenerator;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StrongholdFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(StrongholdFeature.class)
public abstract class StrongholdFeatureMixin {

    @Redirect(
            method = {
                    "shouldStartAt",
                    "locateStructure"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/StrongholdFeature;stateStillValid:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean generateStartPositions(StrongholdFeature instance, @Local(argsOnly = true) ChunkGenerator<?> generator) {
        return ((SQChunkGenerator) generator).seedqueue$hasStrongholds();
    }

    @WrapOperation(
            method = {
                    "shouldStartAt",
                    "locateStructure",
                    "initialize"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/StrongholdFeature;startPositions:[Lnet/minecraft/util/math/ChunkPos;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ChunkPos[] getGeneratorStartPositions(StrongholdFeature instance, Operation<ChunkPos[]> original, @Local(argsOnly = true) ChunkGenerator<?> generator) {
        return ((SQChunkGenerator) generator).seedqueue$getStartPositions();
    }

    @WrapOperation(
            method = "initialize",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/StrongholdFeature;startPositions:[Lnet/minecraft/util/math/ChunkPos;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setGeneratorStartPositions(StrongholdFeature instance, ChunkPos[] startPositions, Operation<Void> original, @Local(argsOnly = true) ChunkGenerator<?> generator) {
        ((SQChunkGenerator) generator).seedqueue$setStartPositions(startPositions);
    }

    @Redirect(
            method = "initialize",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/StrongholdFeature;starts:Ljava/util/List;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private List<StructureStart> getGeneratorStarts(StrongholdFeature instance, @Local(argsOnly = true) ChunkGenerator<?> generator) {
        return ((SQChunkGenerator) generator).seedqueue$getStarts();
    }

    /**
     * @author contaria
     * @reason State is saved per ChunkGenerator, so there is nothing to invalidate.
     */
    @Overwrite
    private void invalidateState() {
    }
}
