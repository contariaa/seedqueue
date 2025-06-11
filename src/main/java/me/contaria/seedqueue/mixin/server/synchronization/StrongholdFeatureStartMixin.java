package me.contaria.seedqueue.mixin.server.synchronization;

import com.llamalad7.mixinextras.sugar.Local;
import me.contaria.seedqueue.interfaces.SQChunkGenerator;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StrongholdFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(StrongholdFeature.Start.class)
public abstract class StrongholdFeatureStartMixin {

    @Redirect(
            method = "initialize",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/feature/StrongholdFeature;method_16598(Lnet/minecraft/world/gen/feature/StrongholdFeature;)Ljava/util/List;"
            )
    )
    private List<StructureStart> getGeneratorStarts(StrongholdFeature instance, @Local(argsOnly = true) ChunkGenerator<?> generator) {
        return ((SQChunkGenerator) generator).seedqueue$getStarts();
    }
}
