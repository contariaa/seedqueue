package me.contaria.seedqueue.mixin.client.render;

import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.SeedQueueEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    @Final
    private Window window;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiler/DisableableProfiler;pop()V",
                    ordinal = 0
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/render/GameRenderer;render(FJZ)V"
                    )
            )
    )
    private void drawSeedQueueChunkMaps(CallbackInfo ci) {
        if (SeedQueue.isOnWall() || !SeedQueue.config.showChunkMaps) {
            return;
        }

        int x = 3;
        int y = 3;
        for (SeedQueueEntry seedQueueEntry : SeedQueue.getEntries()) {
            if (seedQueueEntry.isPaused()) {
                continue;
            }
            WorldGenerationProgressTracker tracker = seedQueueEntry.getWorldGenerationProgressTracker();
            if (tracker == null) {
                continue;
            }
            if (x + tracker.getSize() > this.window.getScaledWidth() - 3) {
                x = 3;
                y += tracker.getSize() + 3;
            }
            LevelLoadingScreen.drawChunkMap(tracker, x + tracker.getSize() / 2, y + tracker.getSize() / 2, 1, 0);
            x += tracker.getSize() + 3;
        }
    }
}
