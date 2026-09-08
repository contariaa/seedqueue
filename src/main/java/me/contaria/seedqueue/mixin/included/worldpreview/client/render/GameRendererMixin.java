package me.contaria.seedqueue.mixin.included.worldpreview.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    private MinecraftClient client;

    @ModifyExpressionValue(
            method = "getFov",
            at = {
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/render/GameRenderer;lastMovementFovMultiplier:F",
                            opcode = Opcodes.GETFIELD
                    ),
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/render/GameRenderer;movementFovMultiplier:F",
                            opcode = Opcodes.GETFIELD
                    )
            },
            require = 2
    )
    private float modifyMovementFovMultiplier(float movementFovMultiplier) {
        if (WorldPreview.renderingPreview) {
            return Math.min(Math.max(WorldPreview.properties.player.getSpeed(), 0.1f), 1.5f);
        }
        return movementFovMultiplier;
    }

    @ModifyExpressionValue(
            method = "updateFog",
            at = {
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/render/GameRenderer;fogColor:F",
                            opcode = Opcodes.GETFIELD
                    ),
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/render/GameRenderer;prevFogColor:F",
                            opcode = Opcodes.GETFIELD
                    )
            }
    )
    private float modifyFogColor(float fogColor) {
        if (WorldPreview.renderingPreview) {
            float brightness = this.client.world.getBrightness(new BlockPos(this.client.getCameraEntity()));
            float chunkDistance = this.client.options.viewDistance / 32.0F;
            return brightness * (1.0F - chunkDistance) + chunkDistance;
        }
        return fogColor;
    }
}
