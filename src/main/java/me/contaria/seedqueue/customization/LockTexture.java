package me.contaria.seedqueue.customization;

import me.contaria.seedqueue.SeedQueue;
import me.contaria.speedrunapi.util.IdentifierUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LockTexture extends AnimatedTexture {
    private final int width;
    private final int height;
    private final LockTextureMetadata metadata;

    public LockTexture(Identifier id) throws IOException {
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

        this.metadata = metadata;
    }

    public double getAspectRatio() {
        return (double) this.width / this.height;
    }

    public int getWeight() {
        return this.metadata.getWeight();
    }

    public static List<LockTexture> createLockTextures() {
        List<LockTexture> lockTextures = new ArrayList<>();
        Identifier lock = IdentifierUtil.of("seedqueue", "textures/gui/wall/lock.png");
        do {
            try {
                lockTextures.add(new LockTexture(lock));
            } catch (IOException e) {
                SeedQueue.LOGGER.warn("Failed to read lock image texture: {}", lock, e);
            }
        } while (MinecraftClient.getInstance().getResourceManager().containsResource(lock = IdentifierUtil.of("seedqueue", "textures/gui/wall/lock-" + lockTextures.size() + ".png")));
        return lockTextures;
    }
}
