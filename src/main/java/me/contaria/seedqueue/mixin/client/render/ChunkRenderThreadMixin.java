package me.contaria.seedqueue.mixin.client.render;

import com.llamalad7.mixinextras.sugar.Cancellable;
import me.contaria.seedqueue.interfaces.SQChunkBuilder;
import me.contaria.seedqueue.interfaces.SQChunkRenderThread;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.world.ChunkRenderThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkRenderThread.class)
public abstract class ChunkRenderThreadMixin implements SQChunkRenderThread {
    @Unique
    private volatile boolean shouldStop;

    @Redirect(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/chunk/ChunkBuilder;takeRebuildQueue()Lnet/minecraft/client/world/ChunkBuilder;"
            )
    )
    private net.minecraft.client.world.ChunkBuilder pollRebuildQueue(ChunkBuilder builder, @Cancellable CallbackInfo ci) throws InterruptedException {
        while (!this.shouldStop) {
            net.minecraft.client.world.ChunkBuilder polled = ((SQChunkBuilder) builder).seedqueue$pollRebuildQueue(500);
            if (polled != null) {
                return polled;
            }
        }
        ci.cancel();
        return null;
    }

    @Override
    public void seedqueue$stop() {
        this.shouldStop = true;
    }
}
