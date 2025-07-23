package me.contaria.seedqueue.customization;

import me.contaria.seedqueue.SeedQueue;
import me.contaria.speedrunapi.util.IdentifierUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class AnimatedTexture {
    protected final List<Identifier> ids = new ArrayList<>();
    protected final List<@Nullable AnimationResourceMetadata> animations = new ArrayList<>();
    private int totalAnimationDuration = 0;

    protected AnimatedTexture(List<Identifier> ids) {
        for (Identifier id : ids) {
            if (!MinecraftClient.getInstance().getResourceManager().containsResource(id)) {
                continue;
            }
            try {
                AnimationResourceMetadata animation = MinecraftClient.getInstance().getResourceManager().getResource(id).getMetadata(AnimationResourceMetadata.READER);
                this.ids.add(id);
                this.animations.add(animation);
                if (animation == null) {
                    continue;
                }
                this.totalAnimationDuration += animation.getDefaultFrameTime() * animation.getFrameCount();
            } catch (IOException e) {
                SeedQueue.LOGGER.warn("Failed to read animation data for {}!", id, e);
            }
        }
        this.totalAnimationDuration = Math.max(1, this.totalAnimationDuration); // avoid division by zero
    }

    public AnimationFrameMetadata getFrame(int tick) {
        // does not currently support setting frametime for individual frames
        // see AnimationFrameResourceMetadata#usesDefaultFrameTime
        int animationPosition = tick % this.totalAnimationDuration;
        for (int i = 0; i < this.animations.size(); i++) {
            AnimationResourceMetadata animation = this.animations.get(i);
            if (animation == null) {
                continue;
            }
            int duration = animation.getDefaultFrameTime() * animation.getFrameCount();
            if (animationPosition < duration) {
                return new AnimationFrameMetadata(this.ids.get(i), animation.getFrameIndexSet().size(), animation.getFrameIndex(animationPosition / animation.getDefaultFrameTime()));
            }
            animationPosition -= duration;
        }
        return new AnimationFrameMetadata(this.ids.get(0), 1, 0);
    }

    @Nullable
    public static AnimatedTexture ofChunks(String namespace, String pathPrefix, String pathSuffix) {
        List<Identifier> chunks = new ArrayList<>();
        Identifier chunk = IdentifierUtil.of(namespace, pathPrefix + pathSuffix);
        do {
            chunks.add(chunk);
        } while (MinecraftClient.getInstance().getResourceManager().containsResource(chunk = IdentifierUtil.of(namespace, pathPrefix + "-" + chunks.size() + pathSuffix)));
        return AnimatedTexture.of(chunks);
    }

    @Nullable
    public static AnimatedTexture of(List<Identifier> ids) {
        AnimatedTexture texture = new AnimatedTexture(ids);
        if (texture.ids.isEmpty()) {
            SeedQueue.LOGGER.warn("No valid animated textures found for identifiers: {}", ids);
            return null;
        }
        return texture;
    }
}
