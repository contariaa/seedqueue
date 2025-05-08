package me.contaria.seedqueue.mixin.compat.worldpreview.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.SubtitlesHud;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @WrapWithCondition(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/SubtitlesHud;render()V"
            )
    )
    private boolean doNotRenderSubtitlesOnWall(SubtitlesHud subtitlesHud) {
        return !SeedQueue.isOnWall();
    }

    @WrapWithCondition(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHud;render(I)V"
            )
    )
    private boolean doNotRenderChatOnWall(ChatHud chatHud, int i) {
        return !SeedQueue.isOnWall();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;overlayRemaining:I",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private int doNotRenderOverlayMessageOnWall(int overlayRemaining) {
        if (SeedQueue.isOnWall()) {
            return 0;
        }
        return overlayRemaining;
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;titleRemainTicks:I",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private int doNotRenderTitleMessageOnWall(int titleRemainTicks) {
        if (SeedQueue.isOnWall()) {
            return 0;
        }
        return titleRemainTicks;
    }

    @ModifyVariable(
            method = "renderStatusBars",
            at = @At("STORE")
    )
    private boolean doNotRenderBlinkingHeartsOnWall(boolean blinking) {
        return blinking && !SeedQueue.isOnWall();
    }
}
