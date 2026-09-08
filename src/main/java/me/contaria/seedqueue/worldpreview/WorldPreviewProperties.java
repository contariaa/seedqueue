package me.contaria.seedqueue.worldpreview;

import com.mojang.blaze3d.platform.GlStateManager;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.mixin.included.worldpreview.accessor.ClientWorldAccessor;
import me.contaria.seedqueue.mixin.included.worldpreview.accessor.EntityAccessor;
import me.contaria.seedqueue.mixin.included.worldpreview.accessor.GameRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;

public class WorldPreviewProperties extends DrawableHelper {
    public final ClientWorld world;
    public final ClientPlayerEntity player;
    public final ClientPlayerInteractionManager interactionManager;
    public final Queue<Packet<?>> packetQueue;
    private final int perspective;

    private int frameCount;

    public WorldPreviewProperties(ClientWorld world, ClientPlayerEntity player, ClientPlayerInteractionManager interactionManager, Queue<Packet<?>> packetQueue, int perspective) {
        this.world = world;
        this.player = player;
        this.interactionManager = interactionManager;
        this.packetQueue = packetQueue;
        this.perspective = perspective;
    }

    /**
     * Sets {@link WorldPreview} properties to the values stored in this {@link WorldPreviewProperties}.
     */
    public void run(Consumer<WorldPreviewProperties> consumer) {
        MinecraftClient client = MinecraftClient.getInstance();

        WorldRenderer mcWorldRenderer = client.worldRenderer;
        ClientPlayerEntity mcPlayer = client.player;
        ClientWorld mcWorld = client.world;
        Entity mcCameraEntity = client.getCameraEntity();
        ClientPlayerInteractionManager mcInteractionManager = client.interactionManager;
        int mcPerspective = client.options.perspective;

        try {
            WorldPreview.renderingPreview = true;

            client.worldRenderer = WorldPreview.worldRenderer;
            client.player = this.player;
            client.world = this.world;
            client.setCameraEntity(this.player);
            client.interactionManager = this.interactionManager;
            client.options.perspective = this.perspective;

            consumer.accept(this);
        } finally {
            WorldPreview.renderingPreview = false;

            client.worldRenderer = mcWorldRenderer;
            client.player = mcPlayer;
            client.world = mcWorld;
            client.setCameraEntity(mcCameraEntity);
            client.interactionManager = mcInteractionManager;
            client.options.perspective = mcPerspective;
        }
    }

    public void render(int mouseX, int mouseY, List<ButtonWidget> buttons, int width, int height) {
        this.tickPackets();
        this.tickEntities();
        this.tickWorld();
        this.renderWorld();
        this.renderHud();
        this.renderMenu(mouseX, mouseY, buttons, width, height);
    }

    public void tickPackets() {
        Profiler profiler = MinecraftClient.getInstance().profiler;
        int dataLimit = this.getDataLimit();
        int applied = 0;

        profiler.swap("tick_packets");
        while (this.shouldApplyPacket(this.packetQueue.peek(), dataLimit, applied++)) {
            //noinspection unchecked
            Packet<ClientPlayPacketListener> packet = (Packet<ClientPlayPacketListener>) Objects.requireNonNull(this.packetQueue.poll());
            profiler.push(packet.getClass().getSimpleName());
            packet.apply(this.player.networkHandler);
            profiler.pop();
        }
    }

    public void tickWorld() {
        Profiler profiler = MinecraftClient.getInstance().profiler;
        ClientWorldAccessor world = (ClientWorldAccessor) this.world;

        profiler.swap("chunkCache");
        world.worldpreview$getClientChunkCache().tickChunks();
        profiler.swap("blocks");
        world.worldpreview$tickBlocks();
    }

    protected boolean shouldApplyPacket(Packet<?> packet, int dataLimit, int applied) {
        return packet != null && (dataLimit >= 100 || dataLimit > applied || !this.canStopAtPacket(packet));
    }

    protected boolean canStopAtPacket(Packet<?> packet) {
        return packet instanceof ChunkDataS2CPacket || packet instanceof MobSpawnS2CPacket || packet instanceof EntitySpawnS2CPacket;
    }

    protected int getDataLimit() {
        return SeedQueue.config.previewDataLimit;
    }

    public void tickEntities() {
        Profiler profiler = MinecraftClient.getInstance().profiler;

        profiler.swap("spawn_player");
        if (!this.world.loadedEntities.contains(this.player)) {
            this.world.spawnEntity(this.player);
        }
        profiler.swap("tick_new_entities");
        for (Entity entity : this.world.entities) {
            if (!((EntityAccessor) entity).worldpreview$isFirstUpdate() || entity.vehicle != null && ((EntityAccessor) entity.vehicle).worldpreview$isFirstUpdate()) {
                continue;
            }
            this.tickEntity(entity);
            for (Entity passenger : this.getPassengersDeep(entity)) {
                this.tickEntity(passenger);
            }
        }
    }

    private List<Entity> getPassengersDeep(Entity entity) {
        List<Entity> passengers = new ArrayList<>();
        while (entity.rider != null) {
            passengers.add(entity.rider);
            entity = entity.rider;
        }
        return passengers;
    }

    private void tickEntity(Entity entity) {
        Profiler profiler = MinecraftClient.getInstance().profiler;
        profiler.push(entity.getClass().getSimpleName());
        if (entity.vehicle != null) {
            entity.vehicle.updatePassengerPosition();
            entity.updatePositionAndAngles(entity.x, entity.y, entity.z, entity.yaw, entity.pitch);
        }
        entity.baseTick();
        profiler.pop();
    }

    public void renderWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        Profiler profiler = client.profiler;

        profiler.swap("render_preview");

        GlStateManager.clear(256);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0, client.width, client.height, 0.0, 1000.0, 3000.0);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, 0.0F);

        profiler.push("light_map");
        ((GameRendererAccessor) client.gameRenderer).worldpreview$tickLightmap();
        profiler.swap("render_world");
        client.gameRenderer.renderWorld(1.0F, System.nanoTime());
        profiler.swap("entity_outlines");
        profiler.pop();

        GlStateManager.clear(256);
    }

    public void buildChunks() {
        MinecraftClient client = MinecraftClient.getInstance();
        Profiler profiler = client.profiler;

        profiler.swap("build_preview");

        GlStateManager.clear(256);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0, client.width, client.height, 0.0, 1000.0, 3000.0);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, 0.0F);

        this.buildChunksInternal();

        GlStateManager.clear(256);
    }

    private void buildChunksInternal() {
        MinecraftClient client = MinecraftClient.getInstance();
        Profiler profiler = client.profiler;

        GlStateManager.enableDepthTest();
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.5F);
        GlStateManager.enableCull();
        GlStateManager.clear(256);

        profiler.push("camera");
        ((GameRendererAccessor) client.gameRenderer).worldpreview$setupCamera(1.0f, 2);
        Camera.update(client.player, client.options.perspective == 2);

        profiler.swap("frustum");
        Frustum.getInstance();

        profiler.swap("culling");
        CameraView cameraView = new CullingCameraView();
        Entity entity = client.getCameraEntity();
        cameraView.setPos(entity.x, entity.y, entity.z);

        GlStateManager.shadeModel(7425);

        profiler.swap("prepareterrain");
        client.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
        DiffuseLighting.disable();
        profiler.swap("terrain_setup");
        client.worldRenderer.setupTerrain(entity, 1.0f, cameraView, this.frameCount++, client.player.isSpectator());
        profiler.swap("updatechunks");
        client.worldRenderer.updateChunks(System.nanoTime());

        profiler.pop();

        GlStateManager.disableDepthTest();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableBlend();
        GlStateManager.clear(256);
    }

    public void renderHud() {
        MinecraftClient client = MinecraftClient.getInstance();
        Profiler profiler = client.profiler;

        GlStateManager.clear(256);
        client.gameRenderer.setupHudMatrixMode();

        profiler.push("ingame_hud");
        client.inGameHud.render(0.0F);
        profiler.pop();

        GlStateManager.clear(256);
    }

    public void renderMenu(int mouseX, int mouseY, List<ButtonWidget> buttons, int width, int height) {
        this.fillGradient(0, 0, width, height + 1, -1072689136, -804253680);
        for (ButtonWidget button : buttons) {
            button.render(MinecraftClient.getInstance(), mouseX, mouseY);
        }
    }

    public static List<ButtonWidget> createMenu(int width, int height) {
        List<ButtonWidget> buttons = new ArrayList<>();
        int i = -16;
        buttons.add(new ButtonWidget(1, width / 2 - 100, height / 4 + 120 + i, I18n.translate("menu.returnToMenu")));
        buttons.add(new ButtonWidget(4, width / 2 - 100, height / 4 + 24 + i, I18n.translate("menu.returnToGame")));
        buttons.add(new ButtonWidget(0, width / 2 - 100, height / 4 + 96 + i, 98, 20, I18n.translate("menu.options")));
        buttons.add(new ButtonWidget(7, width / 2 + 2, height / 4 + 96 + i, 98, 20, I18n.translate("menu.shareToLan")));
        buttons.add(new ButtonWidget(5, width / 2 - 100, height / 4 + 48 + i, 98, 20, I18n.translate("gui.achievements")));
        buttons.add(new ButtonWidget(6, width / 2 + 2, height / 4 + 48 + i, 98, 20, I18n.translate("gui.stats")));
        return buttons;
    }

    public int getPerspective() {
        return this.perspective;
    }
}
