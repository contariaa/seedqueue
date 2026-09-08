package me.contaria.seedqueue.mixin.client.render;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @WrapWithCondition(
            method = "renderWorld(IFJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;viewport(IIII)V"
            )
    )
    private boolean doNotSetViewportOnWall(int x, int y, int width, int height) {
        return !SeedQueue.isOnWall();
    }

    @ModifyArg(
            method = "renderWorld(IFJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;clear(I)V",
                    ordinal = 0
            )
    )
    private int doNotClearOnWall(int mode) {
        if (SeedQueue.isOnWall()) {
            return 256;
        }
        return mode;
    }
}
