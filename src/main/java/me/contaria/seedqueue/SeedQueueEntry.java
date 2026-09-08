package me.contaria.seedqueue;

import me.contaria.seedqueue.compat.SeedQueuePreviewFrameBuffer;
import me.contaria.seedqueue.compat.SeedQueueSettingsCache;
import me.contaria.seedqueue.debug.SeedQueueProfiler;
import me.contaria.seedqueue.fastreset.interfaces.FRMinecraftServer;
import me.contaria.seedqueue.interfaces.SQMinecraftServer;
import me.contaria.seedqueue.mixin.accessor.MinecraftServerAccessor;
import me.contaria.seedqueue.worldpreview.WorldPreviewProperties;
import me.contaria.seedqueue.worldpreview.interfaces.WPMinecraftServer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.SaveHandler;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the {@link MinecraftServer} and any other resources related to a seed in the queue.
 */
public class SeedQueueEntry {
    private IntegratedServer server;

    // will be created lazily when using wall, see MinecraftClientMixin
    private final SaveHandler saveHandler;
    private final LevelProperties levelProperties;
    private final LevelInfo levelInfo;

    @Nullable
    private WorldPreviewProperties previewProperties;
    @Nullable
    private SeedQueuePreviewFrameBuffer frameBuffer;

    @Nullable
    private SeedQueueSettingsCache settingsCache;
    private int perspective;

    private volatile boolean locked;
    private volatile boolean loaded;
    private volatile boolean discarded;

    /**
     * Stores the position (index) of the queue entry in the wall screen's main group.
     * A value of -1 indicates that this entry is not in the main group.
     */
    public int mainPosition = -1;

    public SeedQueueEntry(IntegratedServer server, SaveHandler saveHandler, LevelProperties levelProperties, LevelInfo levelInfo) {
        this.server = server;
        this.saveHandler = saveHandler;
        this.levelProperties = levelProperties;
        this.levelInfo = levelInfo;

        ((SQMinecraftServer) server).seedQueue$setEntry(this);
    }

    public IntegratedServer getServer() {
        return this.server;
    }

    public SaveHandler getSaveHandler() {
        return this.saveHandler;
    }

    public LevelProperties getLevelProperties() {
        return this.levelProperties;
    }

    public LevelInfo getLevelInfo() {
        return this.levelInfo;
    }

    public @Nullable WorldPreviewProperties getPreviewProperties() {
        return this.previewProperties;
    }

    public synchronized void setPreviewProperties(@Nullable WorldPreviewProperties previewProperties) {
        this.previewProperties = previewProperties;
    }

    public SeedQueuePreviewFrameBuffer getFrameBuffer() {
        if (!MinecraftClient.getInstance().isOnThread()) {
            throw new IllegalStateException("Tried to get WorldPreviewFrameBuffer off-thread!");
        }
        if (this.frameBuffer == null) {
            SeedQueueProfiler.push("create_framebuffer");
            this.frameBuffer = new SeedQueuePreviewFrameBuffer();
            SeedQueueProfiler.pop();
        }
        return this.frameBuffer;
    }

    public boolean hasFrameBuffer() {
        return this.frameBuffer != null;
    }

    /**
     * Deletes and removes this entry's framebuffer.
     *
     * @see SeedQueuePreviewFrameBuffer#discard
     */
    public void discardFrameBuffer() {
        if (!MinecraftClient.getInstance().isOnThread()) {
            throw new RuntimeException("Tried to discard WorldPreviewFrameBuffer off-thread!");
        }
        if (this.frameBuffer != null) {
            this.frameBuffer.discard();
            this.frameBuffer = null;
        }
    }

    /**
     * @return True if this entry has either {@link WorldPreviewProperties} or a {@link SeedQueuePreviewFrameBuffer}.
     */
    public boolean hasWorldPreview() {
        return this.previewProperties != null || this.frameBuffer != null;
    }

    public @Nullable SeedQueueSettingsCache getSettingsCache() {
        return this.settingsCache;
    }

    /**
     * Sets the settings cache to be loaded when loading this entry.
     *
     * @throws IllegalStateException If this method is called but {@link SeedQueueEntry#previewProperties} is null.
     */
    public void setSettingsCache(SeedQueueSettingsCache settingsCache) {
        if (this.previewProperties == null) {
            throw new IllegalStateException("Tried to set SettingsCache but SeedQueuePreviewProperties is null!");
        }
        this.settingsCache = settingsCache;
        this.settingsCache.loadPlayerModelParts(this.previewProperties.player);
        this.perspective = this.previewProperties.getPerspective();
    }

    /**
     * Loads this entry's {@link SeedQueueEntry#settingsCache} and {@link SeedQueueEntry#perspective}.
     *
     * @return True if this entry has a settings cache which was loaded.
     */
    public boolean loadSettingsCache() {
        if (this.settingsCache != null) {
            this.settingsCache.load();
            MinecraftClient.getInstance().options.perspective = this.getPerspective();
            return true;
        }
        return false;
    }

    /**
     * @return The perspective used in the preview of this entry.
     */
    public int getPerspective() {
        return this.perspective;
    }

    /**
     * Checks if this entry should pause.
     * <p>
     * Returns true if:
     * <p>
     * - the entry has finished world generation
     * <p>
     * - the entry has been scheduled to pause by the {@link SeedQueueThread}
     *
     * @return If this entry's {@link MinecraftServer} should pause in its current state.
     * @see SQMinecraftServer#seedQueue$shouldPause
     */
    public boolean shouldPause() {
        return ((SQMinecraftServer) this.server).seedQueue$shouldPause();
    }

    /**
     * @return If the entry is currently paused.
     * @see SQMinecraftServer#seedQueue$isPaused
     * @see SeedQueueEntry#shouldPause
     */
    public boolean isPaused() {
        return ((SQMinecraftServer) this.server).seedQueue$isPaused();
    }

    /**
     * @return If the entry has been scheduled to pause by the {@link SeedQueueThread} but hasn't been paused yet.
     * @see SQMinecraftServer#seedQueue$isScheduledToPause
     * @see SeedQueueEntry#shouldPause
     */
    public boolean isScheduledToPause() {
        return ((SQMinecraftServer) this.server).seedQueue$isScheduledToPause();
    }

    /**
     * Schedules this entry to be paused.
     *
     * @see SQMinecraftServer#seedQueue$schedulePause
     */
    public void schedulePause() {
        ((SQMinecraftServer) this.server).seedQueue$schedulePause();
    }

    /**
     * @return True if the entry is not currently paused or scheduled to pause.
     */
    public boolean canPause() {
        return !this.isScheduledToPause() && !this.isPaused();
    }

    /**
     * Unpauses this entry.
     *
     * @see SQMinecraftServer#seedQueue$unpause
     */
    public void unpause() {
        ((SQMinecraftServer) this.server).seedQueue$unpause();
    }

    /**
     * @return True if this entry is currently paused or scheduled to be paused and is allowed to be unpaused.
     */
    public boolean canUnpause() {
        return this.isScheduledToPause() || (this.isPaused() && !this.shouldPause());
    }

    /**
     * @return True if the entry was paused and has now been successfully unpaused.
     * @see SeedQueueEntry#unpause
     * @see SeedQueueEntry#canUnpause
     */
    public boolean tryToUnpause() {
        synchronized (this.server) {
            if (this.canUnpause()) {
                this.unpause();
                return true;
            }
            return false;
        }
    }

    /**
     * @return True if the {@link MinecraftServer} has fully finished generation and is ready to be joined by the player.
     */
    public boolean isReady() {
        return (!SeedQueue.config.generateFakePreview || this.locked) && this.server.isLoading();
    }

    /**
     * @see SeedQueueEntry#lock
     */
    public boolean isLocked() {
        return this.locked;
    }

    /**
     * Locks this entry from being mass-reset on the Wall Screen.
     * Mass Resets include Reset All, Focus Reset, Reset Row, Reset Column.
     *
     * @return True if the entry was not locked before.
     */
    public boolean lock() {
        if (!this.locked) {
            this.restartSeedQueueEntry();
            this.locked = true;
            SeedQueue.ping();
            return true;
        }
        return false;
    }

    private void restartSeedQueueEntry() {
        if (!SeedQueue.config.generateFakePreview) {
            return;
        }

        SeedQueueProfiler.push("stop_server");
        ((FRMinecraftServer) this.server).fastReset$fastReset();
        ((MinecraftServerAccessor) this.server).seedQueue$setRunning(false);

        SeedQueueProfiler.swap("unpause");
        this.unpause();

        SeedQueueProfiler.swap("recreate");
        IntegratedServer server = new IntegratedServer(MinecraftClient.getInstance(), this.levelProperties.getLevelName(), this.levelProperties.getLevelName(), this.levelInfo);
        ((SQMinecraftServer) server).seedQueue$setEntry(this);
        ((WPMinecraftServer) server).worldpreview$setPreviewSpawnPos(((WPMinecraftServer) this.server).worldpreview$getPreviewSpawnPos());
        server.startServerThread();

        this.server = server;
        SeedQueueProfiler.pop();
    }

    /**
     * @see SeedQueueEntry#load
     */
    public boolean isLoaded() {
        return this.loaded;
    }

    /**
     * Marks this entry as loaded and discards its framebuffer.
     */
    public synchronized void load() {
        synchronized (this.server) {
            if (this.discarded) {
                throw new IllegalStateException("Tried to load \"" + this.server.getLevelName() + "\" but it has already been discarded!");
            }

            this.loaded = true;

            SeedQueueProfiler.push("discard_framebuffer");
            this.discardFrameBuffer();

            SeedQueueProfiler.swap("unpause");
            this.unpause();
            SeedQueueProfiler.pop();
        }
    }

    /**
     * @see SeedQueueEntry#discard
     */
    public boolean isDiscarded() {
        return this.discarded;
    }

    /**
     * Discards this entry and all the resources attached to it, including shutting down the {@link MinecraftServer}.
     */
    public synchronized void discard() {
        synchronized (this.server) {
            if (this.discarded) {
                SeedQueue.LOGGER.warn("Tried to discard \"{}\" but it has already been discarded!", this.server.getLevelName());
                return;
            }

            SeedQueue.LOGGER.info("Discarding \"{}\"...", this.server.getLevelName());

            this.discarded = true;

            SeedQueueProfiler.push("discard_framebuffer");
            this.discardFrameBuffer();

            SeedQueueProfiler.swap("stop_server");
            ((FRMinecraftServer) this.server).fastReset$fastReset();
            ((MinecraftServerAccessor) this.server).seedQueue$setRunning(false);

            SeedQueueProfiler.swap("unpause");
            this.unpause();
            SeedQueueProfiler.pop();
        }
    }

    public int getProgressPercentage() {
        return this.server.progress;
    }
}
