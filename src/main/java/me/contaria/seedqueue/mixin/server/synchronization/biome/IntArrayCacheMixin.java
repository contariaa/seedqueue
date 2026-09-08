package me.contaria.seedqueue.mixin.server.synchronization.biome;

import net.minecraft.util.collection.IntArrayCache;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(IntArrayCache.class)
public abstract class IntArrayCacheMixin {
    @Unique
    private static final ThreadLocal<Integer> threadedSize = ThreadLocal.withInitial(() -> 256);
    @Unique
    private static final ThreadLocal<List<int[]>> threadedTcache = ThreadLocal.withInitial(ArrayList::new);
    @Unique
    private static final ThreadLocal<List<int[]>> threadedTallocated = ThreadLocal.withInitial(ArrayList::new);
    @Unique
    private static final ThreadLocal<List<int[]>> threadedCache = ThreadLocal.withInitial(ArrayList::new);
    @Unique
    private static final ThreadLocal<List<int[]>> threadedAllocated = ThreadLocal.withInitial(ArrayList::new);

    @Redirect(
            method = "get",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/collection/IntArrayCache;size:I",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static int useThreadLocalSize() {
        return threadedSize.get();
    }

    @Redirect(
            method = "get",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/collection/IntArrayCache;size:I",
                    opcode = Opcodes.PUTSTATIC
            )
    )
    private static void setThreadLocalSize(int value) {
        threadedSize.set(value);
    }

    @Redirect(
            method = {
                    "get",
                    "clear"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/collection/IntArrayCache;tcache:Ljava/util/List;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static List<int[]> useThreadLocalTcache() {
        return threadedTcache.get();
    }

    @Redirect(
            method = {
                    "get",
                    "clear"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/collection/IntArrayCache;tallocated:Ljava/util/List;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static List<int[]> useThreadLocalTallocated() {
        return threadedTallocated.get();
    }

    @Redirect(
            method = {
                    "get",
                    "clear"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/collection/IntArrayCache;cache:Ljava/util/List;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static List<int[]> useThreadLocalCache() {
        return threadedCache.get();
    }

    @Redirect(
            method = {
                    "get",
                    "clear"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/collection/IntArrayCache;allocated:Ljava/util/List;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static List<int[]> useThreadLocalAllocated() {
        return threadedAllocated.get();
    }
}
