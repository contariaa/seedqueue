package me.contaria.seedqueue.mixin.server.optimization;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.interfaces.SQMinecraftServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.DataInputStream;
import java.io.File;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {

    @WrapOperation(
            method = "loadChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/RegionIo;read(Ljava/io/File;II)Ljava/io/DataInputStream;"
            )
    )
    private DataInputStream skipGettingNbtInQueue(File worldDir, int x, int z, Operation<DataInputStream> original) {
        if (((SQMinecraftServer) MinecraftServer.getServer()).seedQueue$inQueue()) {
            return null;
        }
        return original.call(worldDir, x, z);
    }
}
