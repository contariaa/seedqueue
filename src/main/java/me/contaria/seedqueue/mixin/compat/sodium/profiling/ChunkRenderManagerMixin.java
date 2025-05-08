package me.contaria.seedqueue.mixin.compat.sodium.profiling;

import me.contaria.seedqueue.debug.SeedQueueProfiler;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Profiling mixins add more usage of the profiler to hot paths during wall rendering.
 * These Mixins will be removed in later versions of SeedQueue.
 */
@Mixin(value = ChunkRenderManager.class, remap = false, priority = 500)
public abstract class ChunkRenderManagerMixin {

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder;<init>(Lme/jellysquid/mods/sodium/client/gl/attribute/GlVertexFormat;Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderBackend;)V",
                    remap = false
            ),
            remap = true
    )
    private void profileCreateChunkBuilder(CallbackInfo ci) {
        SeedQueueProfiler.push("create_chunk_builder");
    }

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder;init(Lnet/minecraft/client/world/ClientWorld;Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPassManager;)V"
            ),
            remap = true
    )
    private void profileInitChunkBuilder(CallbackInfo ci) {
        SeedQueueProfiler.swap("init_chunk_builder");
    }

    @Inject(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderManager;dirty:Z",
                    remap = false
            ),
            remap = true
    )
    private void profileCreateRenderLists(CallbackInfo ci) {
        SeedQueueProfiler.swap("create_render_lists");
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = true
    )
    private void profilePop_init(CallbackInfo ci) {
        SeedQueueProfiler.pop();
    }

    @Inject(
            method = "updateChunks",
            at = @At("HEAD")
    )
    private void profileImportantRebuilds(CallbackInfo ci) {
        SeedQueueProfiler.push("important_rebuilds");
    }

    @Inject(
            method = "updateChunks",
            at = @At(
                    value = "FIELD",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderManager;rebuildQueue:Lit/unimi/dsi/fastutil/objects/ObjectArrayFIFOQueue;",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private void profileRebuilds(CallbackInfo ci) {
        SeedQueueProfiler.swap("rebuilds");
    }

    @Inject(
            method = "updateChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder;performPendingUploads()Z"
            )
    )
    private void profilePendingUploads(CallbackInfo ci) {
        SeedQueueProfiler.swap("pending_uploads");
    }

    @Inject(
            method = "updateChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderBackend;upload(Ljava/util/Iterator;)V"
            )
    )
    private void profileBackendUpload(CallbackInfo ci) {
        SeedQueueProfiler.swap("backend_upload");
    }

    @Inject(
            method = "updateChunks",
            at = @At("RETURN")
    )
    private void profilePop_updateChunks(CallbackInfo ci) {
        SeedQueueProfiler.pop();
    }

    @Inject(
            method = "destroy",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderManager;resetGraph()V"
            )
    )
    private void profileReset(CallbackInfo ci) {
        SeedQueueProfiler.push("reset");
    }

    @Inject(
            method = "destroy",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectOpenHashMap;values()Lit/unimi/dsi/fastutil/objects/ObjectCollection;"
            )
    )
    private void profileUnloadSections(CallbackInfo ci) {
        SeedQueueProfiler.swap("unload_sections");
    }

    @Inject(
            method = "destroy",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectOpenHashMap;clear()V"
            )
    )
    private void profileClearSections(CallbackInfo ci) {
        SeedQueueProfiler.swap("clear_sections");
    }

    @Inject(
            method = "destroy",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder;stopWorkers()V"
            )
    )
    private void profileStopWorkers(CallbackInfo ci) {
        SeedQueueProfiler.swap("stop_workers");
    }

    @Inject(
            method = "destroy",
            at = @At("RETURN")
    )
    private void profilePop_destroy(CallbackInfo ci) {
        SeedQueueProfiler.pop();
    }

    @Inject(
            method = "resetGraph",
            at = @At(
                    value = "FIELD",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderManager;rebuildQueue:Lit/unimi/dsi/fastutil/objects/ObjectArrayFIFOQueue;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private void profileClearRebuildQueue(CallbackInfo ci) {
        SeedQueueProfiler.push("clear_rebuild_queue");
    }

    @Inject(
            method = "resetGraph",
            at = @At(
                    value = "FIELD",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderManager;visibleBlockEntities:Lit/unimi/dsi/fastutil/objects/ObjectList;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private void profileClearBlockEntities(CallbackInfo ci) {
        SeedQueueProfiler.swap("clear_block_entities");
    }

    @Inject(
            method = "resetGraph",
            at = @At(
                    value = "FIELD",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderManager;chunkRenderLists:[Lme/jellysquid/mods/sodium/client/render/chunk/lists/ChunkRenderList;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private void profileResetChunkRenders(CallbackInfo ci) {
        SeedQueueProfiler.swap("reset_chunk_renders");
    }

    @Inject(
            method = "resetGraph",
            at = @At(
                    value = "FIELD",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderManager;tickableChunks:Lit/unimi/dsi/fastutil/objects/ObjectList;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private void profileClearChunks(CallbackInfo ci) {
        SeedQueueProfiler.swap("clear_chunks");
    }

    @Inject(
            method = "resetGraph",
            at = @At("RETURN")
    )
    private void profilePop_reset(CallbackInfo ci) {
        SeedQueueProfiler.pop();
    }

    @Inject(
            method = "updateGraph",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderManager;init(Lnet/minecraft/client/render/Camera;Lme/jellysquid/mods/sodium/client/util/math/FrustumExtended;IZ)V"
            ),
            remap = true
    )
    private void profileInit(CallbackInfo ci) {
        SeedQueueProfiler.push("init");
    }

    @Inject(
            method = "updateGraph",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderManager;init(Lnet/minecraft/client/render/Camera;Lme/jellysquid/mods/sodium/client/util/math/FrustumExtended;IZ)V",
                    shift = At.Shift.AFTER
            ),
            remap = true
    )
    private void profileIterateQueue(CallbackInfo ci) {
        SeedQueueProfiler.swap("iterate_queue");
    }

    @Inject(
            method = "updateGraph",
            at = @At("RETURN"),
            remap = true
    )
    private void profilePop_updateGraph(CallbackInfo ci) {
        SeedQueueProfiler.pop();
    }
}
