package me.contaria.seedqueue.customization;

import me.contaria.seedqueue.SeedQueue;
import me.contaria.speedrunapi.util.IdentifierUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LockTexture extends AnimatedTexture {
    private final int width;
    private final int height;
    private final LockTextureMetadata metadata;

    public LockTexture(Identifier id, int defaultWeight) throws IOException {
        super(id);

        Resource resource = MinecraftClient.getInstance()
            .getResourceManager()
            .getResource(id);

        try (NativeImage image = NativeImage.read(resource.getInputStream())) {
            this.width = image.getWidth();
            this.height = image.getHeight() / (this.animation != null ? this.animation.getFrameIndexSet().size() : 1);
        }

        LockTextureMetadata metadata = resource.getMetadata(LockTextureMetadata.READER);

        if (metadata == null) {
            metadata = new LockTextureMetadata();
        }

        if (metadata.weight == 0) {
            metadata.weight = defaultWeight;
        }

        this.metadata = metadata;
    }

    public double getAspectRatio() {
        return (double) this.width / this.height;
    }

    public int getWeight() {
        return Math.max(1, this.metadata.weight);
    }

    public static List<LockTexture> createLockTextures() {
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

        List<LockTexture> lockTextures = new ArrayList<>();

        Identifier lock = IdentifierUtil.of("seedqueue", "textures/gui/wall/lock.png");

        int defaultWeight = 1;
        try {
            MainLockTextureMetadata metadata = resourceManager
                .getResource(lock)
                .getMetadata(MainLockTextureMetadata.READER);

            if (metadata != null) {
                defaultWeight = metadata.defaultWeight;
            }
        } catch (IOException e) {
            SeedQueue.LOGGER.warn("Failed to read the main lock texture", e);
        }

        do {
            try {
                lockTextures.add(new LockTexture(lock, defaultWeight));
            } catch (IOException e) {
                SeedQueue.LOGGER.warn("Failed to read lock image texture: {}", lock, e);
            }
        } while (resourceManager.containsResource(lock = IdentifierUtil.of("seedqueue", "textures/gui/wall/lock-" + lockTextures.size() + ".png")));
        return lockTextures;
    }
}
