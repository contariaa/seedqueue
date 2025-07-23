package me.contaria.seedqueue.customization;

import net.minecraft.util.Identifier;

public class AnimationFrameMetadata {
    public final Identifier animationId;
    public final int animationFrameCount;
    public final int frameIndex;
    public AnimationFrameMetadata(Identifier animationId, int animationFrameCount, int frameIndex) {
        this.animationId = animationId;
        this.animationFrameCount = animationFrameCount;
        this.frameIndex = frameIndex;
    }
}
