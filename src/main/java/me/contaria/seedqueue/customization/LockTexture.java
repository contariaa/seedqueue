package me.contaria.seedqueue.customization;

import me.contaria.seedqueue.SeedQueue;
import me.contaria.speedrunapi.util.IdentifierUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LockTexture extends AnimatedTexture {
    private final int width;
    private final int height;

    public LockTexture(List<Identifier> ids) throws IOException {
        super(ids);
        try (NativeImage image = NativeImage.read(MinecraftClient.getInstance().getResourceManager().getResource(this.ids.get(0)).getInputStream())) {
            AnimationResourceMetadata animation = this.animations.get(0);
            this.width = image.getWidth();
            this.height = image.getHeight() / (animation != null ? animation.getFrameIndexSet().size() : 1);
        }
    }

    public LockTexture(Identifier id) throws IOException {
        this(Collections.singletonList(id));
    }

    public double getAspectRatio() {
        return (double) this.width / this.height;
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
