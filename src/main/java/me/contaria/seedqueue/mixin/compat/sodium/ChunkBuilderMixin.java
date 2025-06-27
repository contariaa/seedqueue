package me.contaria.seedqueue.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.compat.SodiumCompat;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(value = ChunkBuilder.class, remap = false)
public abstract class ChunkBuilderMixin {
    @Mutable
    @Shadow
    @Final
    private AtomicBoolean running;

    @ModifyReturnValue(
            method = "getMaxThreadCount",
            at = @At("RETURN")
    )
    private static int modifyMaxThreads(int maxThreads) {
        if (SeedQueue.isOnWall()) {
            return SeedQueue.config.getChunkUpdateThreads();
        }
        return maxThreads;
    }

    @ModifyArg(
            method = "createWorker",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Thread;setPriority(I)V"
            )
    )
    private int modifyChunkUpdateThreadPriority(int priority) {
        if (SeedQueue.isOnWall()) {
            return SeedQueue.config.chunkUpdateThreadPriority;
        }
        return priority;
    }

    @WrapWithCondition(
            method = "stopWorkers",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Thread;join()V"
            )
    )
    private boolean doNotWaitForWorkersToStopOnWall(Thread thread) {
        return !SeedQueue.isOnWall();
    }

    // because ChunkBuilderMixin#doNotWaitForWorkersToStopOnWall prevents waiting for old workers to shut down,
    // it's necessary to replace the atomic boolean to prevent the worker threads staying alive
    // when new workers are started before old ones have stopped
    @Inject(
            method = "stopWorkers",
            at = @At("TAIL")
    )
    private void replaceRunningAtomicBooleanOnWall(CallbackInfo ci) {
        if (SeedQueue.isOnWall()) {
            this.running = new AtomicBoolean();
        }
    }

    @WrapOperation(
            method = "createWorker",
            at = @At(
                    value = "NEW",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;"
            )
    )
    private ChunkBuildBuffers loadCachedBuildBuffersOnWall(@Coerce Object passId, BlockRenderPassManager buffers, Operation<ChunkBuildBuffers> original) {
        if (SeedQueue.isOnWall() && !SodiumCompat.WALL_BUILD_BUFFERS_POOL.isEmpty()) {
            return Objects.requireNonNull(SodiumCompat.WALL_BUILD_BUFFERS_POOL.remove(0));
        }
        return original.call(passId, buffers);
    }

    @WrapWithCondition(
            method = "startWorkers",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"
            )
    )
    private boolean suppressStartWorkersLogOnWall(Logger logger, String s, Object o) {
        return !SeedQueue.isOnWall();
    }

    @WrapWithCondition(
            method = "stopWorkers",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"
            )
    )
    private boolean suppressStopWorkersLogOnWall(Logger logger, String s) {
        return !SeedQueue.isOnWall();
    }

    @ModifyExpressionValue(
            method = "getSchedulingBudget",
            at = @At(
                    value = "CONSTANT",
                    args = "intValue=2")
    )
    private int reduceSchedulingBudgetOnWall(int TASK_QUEUE_LIMIT_PER_WORKER) {
        if (SeedQueue.isOnWall()) {
            return 1;
        }
        return TASK_QUEUE_LIMIT_PER_WORKER;
    }
}
