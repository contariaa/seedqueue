package me.contaria.seedqueue.mixin.included.worldpreview.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @WrapWithCondition(
            method = "spawnEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sound/SoundManager;play(Lnet/minecraft/client/sound/SoundInstance;)V"
            )
    )
    private boolean doNotPlayMinecartSoundOnPreview(SoundManager instance, SoundInstance sound) {
        return !WorldPreview.renderingPreview;
    }
}
