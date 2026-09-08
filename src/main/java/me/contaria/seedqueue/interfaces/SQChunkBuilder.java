package me.contaria.seedqueue.interfaces;

public interface SQChunkBuilder {
    net.minecraft.client.world.ChunkBuilder seedqueue$pollRebuildQueue(long timeoutMs) throws InterruptedException;

    void seedqueue$stopThreads();
}
