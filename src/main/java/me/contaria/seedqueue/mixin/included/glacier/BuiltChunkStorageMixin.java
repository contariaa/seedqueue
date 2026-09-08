package me.contaria.seedqueue.mixin.included.glacier;

import net.minecraft.client.render.BuiltChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltChunkStorage.class)
public abstract class BuiltChunkStorageMixin {
    @Unique
    private double lastX = Double.NaN;
    @Unique
    private double lastZ = Double.NaN;

    @Inject(
            method = "updateCameraPosition",
            at = @At("HEAD"),
            cancellable = true
    )
    private void checkIfPositionChanged(double x, double z, CallbackInfo ci) {
        if (this.lastX == x && this.lastZ == z) {
            ci.cancel();
        } else {
            this.lastX = x;
            this.lastZ = z;
        }
    }
}
