package me.contaria.seedqueue.mixin.server.synchronization.biome;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.noise.PerlinNoiseGenerator;
import net.minecraft.world.biome.MesaBiome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MesaBiome.class)
public abstract class MesaBiomeMixin {
    @Unique
    private final ThreadLocal<BlockState[]> threadedLayerBlocks = new ThreadLocal<>();
    @Unique
    private final ThreadLocal<Long> threadedSeed = ThreadLocal.withInitial(() -> 0L);
    @Unique
    private final ThreadLocal<PerlinNoiseGenerator> threadedHeightCutoffNoise = new ThreadLocal<>();
    @Unique
    private final ThreadLocal<PerlinNoiseGenerator> threadedHeightNoise = new ThreadLocal<>();
    @Unique
    private final ThreadLocal<PerlinNoiseGenerator> threadedLayerNoise = new ThreadLocal<>();

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;seed:J",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedSeed(MesaBiome biome, long seed) {
        this.threadedSeed.set(seed);
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;heightCutoffNoise:Lnet/minecraft/util/math/noise/PerlinNoiseGenerator;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedHeightCutoffNoise(MesaBiome biome, PerlinNoiseGenerator heightCutoffNoise) {
        this.threadedHeightCutoffNoise.set(heightCutoffNoise);
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;heightNoise:Lnet/minecraft/util/math/noise/PerlinNoiseGenerator;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedHeightNoise(MesaBiome biome, PerlinNoiseGenerator heightNoise) {
        this.threadedHeightNoise.set(heightNoise);
    }

    @WrapOperation(
            method = "initLayerBlocks",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;layerBlocks:[Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedLayerBlocks(MesaBiome biome, BlockState[] layerBlocks, Operation<Void> original) {
        this.threadedLayerBlocks.set(layerBlocks);
    }

    @Redirect(
            method = "initLayerBlocks",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;layerNoise:Lnet/minecraft/util/math/noise/PerlinNoiseGenerator;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedLayerNoise(MesaBiome biome, PerlinNoiseGenerator layerNoise) {
        this.threadedLayerNoise.set(layerNoise);
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;seed:J",
                    opcode = Opcodes.GETFIELD
            )
    )
    private long getThreadedSeed(MesaBiome biome) {
        return this.threadedSeed.get();
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;heightCutoffNoise:Lnet/minecraft/util/math/noise/PerlinNoiseGenerator;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private PerlinNoiseGenerator getThreadedHeightCutoffNoise(MesaBiome biome) {
        return this.threadedHeightCutoffNoise.get();
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;heightNoise:Lnet/minecraft/util/math/noise/PerlinNoiseGenerator;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private PerlinNoiseGenerator getThreadedHeightNoise(MesaBiome biome) {
        return this.threadedHeightNoise.get();
    }

    @Redirect(
            method = {
                    "method_6420",
                    "initLayerBlocks",
                    "calculateLayerBlockState"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;layerBlocks:[Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BlockState[] getThreadedLayerBlocks(MesaBiome biome) {
        return this.threadedLayerBlocks.get();
    }

    @Redirect(
            method = "calculateLayerBlockState",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/MesaBiome;layerNoise:Lnet/minecraft/util/math/noise/PerlinNoiseGenerator;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private PerlinNoiseGenerator getThreadedLayerNoise(MesaBiome biome) {
        return this.threadedLayerNoise.get();
    }
}
