package me.contaria.seedqueue.gui.wall;

import com.mojang.blaze3d.systems.RenderSystem;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.SeedQueueEntry;
import me.contaria.seedqueue.compat.SeedQueuePreviewFrameBuffer;
import me.contaria.seedqueue.compat.SeedQueuePreviewProperties;
import me.contaria.seedqueue.customization.LockTexture;
import me.contaria.seedqueue.interfaces.SQWorldGenerationProgressTracker;
import me.contaria.seedqueue.mixin.accessor.WorldRendererAccessor;
import me.contaria.speedrunapi.config.SpeedrunConfigAPI;
import me.voidxwalker.autoreset.Atum;
import me.voidxwalker.autoreset.interfaces.ISeedStringHolder;
import me.voidxwalker.worldpreview.WorldPreview;
import me.voidxwalker.worldpreview.WorldPreviewProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SeedQueuePreview extends DrawableHelper {
    private final SeedQueueWallScreen wall;
    private final SeedQueueEntry seedQueueEntry;
    private final WorldGenerationProgressTracker tracker;
    private SeedQueuePreviewProperties previewProperties;
    private WorldRenderer worldRenderer;

    private final MinecraftClient client;

    private final int width;
    private final int height;

    private final List<ButtonWidget> buttons;
    private final boolean showMenu;
    private final String seedString;
    private final LockTexture lockTexture;

    private long cooldownStart;
    private boolean rendered;
    private int lastPreviewFrame;

    public SeedQueuePreview(SeedQueueWallScreen wall, SeedQueueEntry seedQueueEntry) {
        this.wall = wall;
        this.seedQueueEntry = seedQueueEntry;
        this.tracker = Objects.requireNonNull(seedQueueEntry.getWorldGenerationProgressTracker());

        this.client = MinecraftClient.getInstance();

        // forceUnicodeFont is not being loaded from the settings cache because it is not included in SeedQueueSettingsCache.PREVIEW_SETTINGS
        int scale = SeedQueue.config.calculateSimulatedScaleFactor(
                this.seedQueueEntry.getSettingsCache() != null ? (int) this.seedQueueEntry.getSettingsCache().getValue("guiScale") : MinecraftClient.getInstance().options.guiScale,
                MinecraftClient.getInstance().options.forceUnicodeFont
        );
        this.width = (int) Math.ceil((double) SeedQueue.config.simulatedWindowSize.width() / scale);
        this.height = (int) Math.ceil((double) SeedQueue.config.simulatedWindowSize.height() / scale);

        this.buttons = WorldPreviewProperties.createMenu(this.width, this.height, () -> {}, () -> {});
        this.showMenu = !Boolean.TRUE.equals(SpeedrunConfigAPI.getConfigValue("standardsettings", "autoF3Esc"));

        if (Atum.inDemoMode()) {
            this.seedString = "North Carolina";
        } else if (Atum.getSeedProvider().shouldShowSeed()) {
            //noinspection DataFlowIssue
            this.seedString = ((ISeedStringHolder) (Object) this.seedQueueEntry.getLevelInfo()).atum$getSeedString();
        } else {
            this.seedString = "Set Seed";
        }

        this.lockTexture = wall.getRandomLockTexture();

        this.updatePreviewProperties();
    }

    private void updatePreviewProperties() {
        if (this.isOnlyDrawingChunkmap()) {
            return;
        }
        if (this.previewProperties == (this.previewProperties = this.seedQueueEntry.getPreviewProperties())) {
            return;
        }
        if (this.previewProperties != null) {
            this.worldRenderer = SeedQueueWallScreen.getOrCreateWorldRenderer(this.previewProperties.world);
            if (this.seedQueueEntry.getSettingsCache() == null) {
                this.seedQueueEntry.setSettingsCache(this.wall.settingsCache);
            }
        } else {
            this.worldRenderer = null;
        }
    }

    public void render() {
        this.updatePreviewProperties();

        this.wall.setOrtho(this.width, this.height);
        if (this.isOnlyDrawingChunkmap()) {
            this.rendered = this.isChunkmapReady();
        } else if (!this.isPreviewReady()) {
            SeedQueuePreview.renderBackground(this.width, this.height);
            if (this.previewProperties != null) {
                this.buildChunks();
            }
        } else {
            this.renderPreview();
            this.rendered = true;
        }

        if (!this.seedQueueEntry.isReady()) {
            this.renderLoading();
        } else if ((SeedQueue.config.chunkMapFreezing != -1 && !this.seedQueueEntry.isLocked()) || this.isOnlyDrawingChunkmap()) {
            this.renderChunkmap();
        }
        this.wall.resetOrtho();
    }

    private void renderPreview() {
        SeedQueuePreviewFrameBuffer frameBuffer = this.seedQueueEntry.getFrameBuffer();
        if (this.previewProperties != null) {
            if (this.shouldRedrawPreview() && frameBuffer.updateRenderData(this.worldRenderer)) {
                this.redrawPreview(frameBuffer);
            } else {
                this.buildChunks();
            }
        }
        this.wall.setOrtho(this.width, this.height);
        frameBuffer.draw(this.width, this.height);
    }

    private void redrawPreview(SeedQueuePreviewFrameBuffer frameBuffer) {
        frameBuffer.beginWrite();
        // related to WorldRendererMixin#doNotClearOnWallScreen
        // the suppressed call usually renders a light blue overlay over the entire screen,
        // instead we draw it onto the preview ourselves
        DrawableHelper.fill(0, 0, this.width, this.height, -5323025);
        this.run(properties -> properties.render(0, 0, 0.0f, this.buttons, this.width, this.height, this.showMenu));
        frameBuffer.endWrite();

        this.client.getFramebuffer().beginWrite(false);
        this.wall.refreshViewport();
        this.lastPreviewFrame = this.wall.frame;
    }

    private void renderLoading() {
        // see LevelLoadingScreen#render
        this.renderChunkmap();
        this.drawCenteredString(this.client.textRenderer, MathHelper.clamp(this.tracker.getProgressPercentage(), 0, 100) + "%", 45, this.height - 75 - 9 / 2 - 30, 16777215);
        this.drawCenteredString(this.client.textRenderer, this.seedString, 45, this.height - 75 - 9 / 2 - 50, 16777215);
    }

    private void renderChunkmap() {
        WorldGenerationProgressTracker tracker = this.seedQueueEntry.isLocked() ? this.tracker : ((SQWorldGenerationProgressTracker) this.tracker).seedQueue$getFrozenCopy().orElse(this.tracker);
        LevelLoadingScreen.drawChunkMap(tracker, 45, this.height - 75 + 30, 2, 0);
    }

    public void build() {
        this.updatePreviewProperties();
        if (this.previewProperties != null) {
            this.buildChunks();
        }
    }

    private void buildChunks() {
        this.run(properties -> {
            properties.tickPackets();
            properties.tickEntities();
            ((SeedQueuePreviewProperties) properties).buildChunks();
        });
    }

    private void run(Consumer<WorldPreviewProperties> consumer) {
        WorldRenderer worldRenderer = WorldPreview.worldRenderer;
        WorldPreviewProperties properties = WorldPreview.properties;
        try {
            WorldPreview.worldRenderer = this.worldRenderer;
            WorldPreview.properties = this.previewProperties;
            WorldPreview.properties.run(consumer);
        } finally {
            WorldPreview.worldRenderer = worldRenderer;
            WorldPreview.properties = properties;
        }
    }

    private boolean isOnlyDrawingChunkmap() {
        return SeedQueue.config.isChunkmapResetting() && (!this.seedQueueEntry.isLocked() || this.canDrawOnlyChunkmapIfLocked());
    }

    private boolean canDrawOnlyChunkmapIfLocked() {
        return SeedQueue.config.freezeLockedPreviews || (this.wall.layout.locked != null && this.wall.layout.locked.size() == 0);
    }

    private boolean shouldRedrawPreview() {
        return this.lastPreviewFrame == 0 || this.wall.frame - this.lastPreviewFrame >= SeedQueue.config.wallFPS / SeedQueue.config.previewFPS;
    }

    private boolean isChunkmapReady() {
        return ((SQWorldGenerationProgressTracker) this.tracker).seedQueue$shouldFreeze();
    }

    private boolean isPreviewReady() {
        return this.seedQueueEntry.hasFrameBuffer() || (this.worldRenderer != null && ((WorldRendererAccessor) this.worldRenderer).seedQueue$getCompletedChunkCount() > 0);
    }

    public boolean isRenderingReady() {
        return SeedQueue.config.isChunkmapResetting() ? this.isChunkmapReady() : this.isPreviewReady();
    }

    public boolean hasRendered() {
        return this.rendered;
    }

    protected boolean canReset(boolean ignoreLock, boolean ignoreResetCooldown) {
        return this.rendered && (!this.seedQueueEntry.isLocked() || ignoreLock) && (this.isCooldownReady() || ignoreResetCooldown) && !this.seedQueueEntry.isLoaded();
    }

    protected void resetCooldown() {
        this.cooldownStart = Long.MAX_VALUE;
    }

    protected void populateCooldownStart(long cooldownStart) {
        if (this.rendered && this.cooldownStart == Long.MAX_VALUE) {
            this.cooldownStart = cooldownStart;
        }
    }

    private boolean isCooldownReady() {
        return Util.getMeasuringTimeMs() - this.cooldownStart >= SeedQueue.config.resetCooldown;
    }

    public void printDebug() {
        if (this.worldRenderer != null) {
            SeedQueue.LOGGER.info("SeedQueue-DEBUG | Instance: {}, Seed: {}, World Gen %: {}, Chunks: {} ({}), locked: {}, paused: {}, ready: {}", this.seedQueueEntry.getServer().getLevelName(), this.seedQueueEntry.getLevelInfo().getSeed(), this.seedQueueEntry.getProgressPercentage(), this.worldRenderer.getChunksDebugString(), this.worldRenderer.isTerrainRenderComplete(), this.seedQueueEntry.isLocked(), this.seedQueueEntry.isPaused(), this.seedQueueEntry.isReady());
        } else {
            SeedQueue.LOGGER.info("SeedQueue-DEBUG | Instance: {}, Seed: {}, World Gen %: {}", this.seedQueueEntry.getServer().getLevelName(), this.seedQueueEntry.getLevelInfo().getSeed(), this.seedQueueEntry.getProgressPercentage());
        }
    }

    public void printStacktrace() {
        SeedQueue.LOGGER.info("SeedQueue-DEBUG | Instance: {}, Stacktrace: {}", this.seedQueueEntry.getServer().getLevelName(), Arrays.toString(this.seedQueueEntry.getServer().getThread().getStackTrace()));
    }

    public SeedQueueEntry getSeedQueueEntry() {
        return this.seedQueueEntry;
    }

    public LockTexture getLockTexture() {
        return this.lockTexture;
    }

    // see Screen#renderBackground
    @SuppressWarnings("deprecation")
    public static void renderBackground(int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        MinecraftClient.getInstance().getTextureManager().bindTexture(BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(0.0, height, 0.0).texture(0.0F, height / 32.0F).color(64, 64, 64, 255).next();
        buffer.vertex(width, height, 0.0).texture(width / 32.0F, height / 32.0F).color(64, 64, 64, 255).next();
        buffer.vertex(width, 0.0, 0.0).texture(width / 32.0F, 0.0F).color(64, 64, 64, 255).next();
        buffer.vertex(0.0, 0.0, 0.0).texture(0.0F, 0.0F).color(64, 64, 64, 255).next();
        tessellator.draw();
    }
}
