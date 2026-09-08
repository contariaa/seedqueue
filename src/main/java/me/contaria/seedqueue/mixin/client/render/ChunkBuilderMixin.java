package me.contaria.seedqueue.mixin.client.render;

import me.contaria.seedqueue.interfaces.SQChunkBuilder;
import me.contaria.seedqueue.interfaces.SQChunkRenderThread;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.world.ChunkRenderThread;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Mixin(ChunkBuilder.class)
public abstract class ChunkBuilderMixin implements SQChunkBuilder {
    @Shadow
    @Final
    private List<ChunkRenderThread> field_11037;

    @Shadow
    @Final
    private BlockingQueue<net.minecraft.client.world.ChunkBuilder> rebuildQueue;

    @Override
    public net.minecraft.client.world.ChunkBuilder seedqueue$pollRebuildQueue(long timeoutMs) throws InterruptedException {
        return this.rebuildQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void seedqueue$stopThreads() {
        for (ChunkRenderThread thread : this.field_11037) {
            ((SQChunkRenderThread) thread).seedqueue$stop();
        }
    }
}
