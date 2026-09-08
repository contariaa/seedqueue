package me.contaria.seedqueue.mixin.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.SeedQueueEntry;
import me.contaria.seedqueue.interfaces.SQMinecraftServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements SQMinecraftServer {
    @Shadow
    private String levelName;
    @Shadow
    private boolean loading;

    @Unique
    private SeedQueueEntry seedQueueEntry;

    @Unique
    private volatile boolean pauseScheduled;
    @Unique
    private volatile boolean paused;

    @Shadow
    public abstract boolean isOnThread();

    @Shadow
    public abstract PlayerManager getPlayerManager();

    @ModifyExpressionValue(
            method = "startServerThread",
            at = @At(
                    value = "NEW",
                    target = "(Ljava/lang/Runnable;Ljava/lang/String;)Ljava/lang/Thread;"
            )
    )
    private Thread modifyServerThreadProperties(Thread thread) {
        if (SeedQueue.inQueue()) {
            thread.setPriority(SeedQueue.config.serverThreadPriority);
        }
        String name = this.levelName;
        if (name.startsWith("Random Speedrun #") || name.startsWith("Set Speedrun #")) {
            thread.setName(thread.getName() + " " + name.substring(name.indexOf('#')));
        } else {
            thread.setName(thread.getName() + " - " + name);
        }
        return thread;
    }

    @WrapWithCondition(
            method = "logProgress",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"
            )
    )
    private boolean suppressProgressLogInQueue(Logger logger, String string) {
        return this.seedQueueEntry == null || this.seedQueueEntry.isLoaded();
    }

    @WrapWithCondition(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Thread;sleep(J)V"
            )
    )
    private boolean doNotSleepInQueue(long ms) {
        return !this.seedQueue$inQueue();
    }

    @WrapOperation(
            method = "run",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/MinecraftServer;loading:Z",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void pauseServer(MinecraftServer server, boolean value, Operation<Void> original) {
        // "loading" is a bad mapping and actually means something more like "finishedLoading"
        if (this.loading || !this.seedQueue$inQueue()) {
            original.call(server, value);
            return;
        }

        original.call(server, value);

        SeedQueue.LOGGER.info("Finished loading \"{}\".", this.levelName);
        this.seedQueue$tryPausingServer();
        this.seedQueueEntry = null;
    }

    @Override
    public SeedQueueEntry seedQueue$getEntry() {
        return this.seedQueueEntry;
    }

    @Override
    public boolean seedQueue$inQueue() {
        return this.seedQueueEntry != null;
    }

    @Override
    public void seedQueue$setEntry(SeedQueueEntry entry) {
        this.seedQueueEntry = entry;
    }

    @Override
    public boolean seedQueue$shouldPause() {
        SeedQueueEntry entry = this.seedQueueEntry;
        if (entry == null || entry.isLoaded() || entry.isDiscarded()) {
            return false;
        }
        // "loading" is a bad mapping, it means something more like "finishedLoading"
        return this.pauseScheduled || this.loading;
    }

    @Override
    public synchronized void seedQueue$tryPausingServer() {
        if (!this.isOnThread()) {
            throw new IllegalStateException("Tried to pause the server from another thread!");
        }

        if (!this.seedQueue$shouldPause()) {
            return;
        }

        try {
            this.paused = true;
            this.pauseScheduled = false;
            SeedQueue.ping();
            this.wait();
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to pause server in SeedQueue!", e);
        } finally {
            this.paused = false;
        }
    }

    @Override
    public boolean seedQueue$isPaused() {
        return this.paused;
    }

    @Override
    public boolean seedQueue$isScheduledToPause() {
        return this.pauseScheduled;
    }

    @Override
    public synchronized void seedQueue$schedulePause() {
        if (!this.paused) {
            this.pauseScheduled = true;
        }
    }

    @Override
    public synchronized void seedQueue$unpause() {
        this.pauseScheduled = false;
        if (this.paused) {
            this.notify();
            this.paused = false;
        }
    }
}
