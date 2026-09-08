package me.contaria.seedqueue.mixin.server.synchronization.block.bounding_box;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.StemBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StemBlock.class)
public abstract class StemBlockMixin extends BlockMixin {

    @Inject(
            method = "setBoundingBox",
            at = @At("HEAD")
    )
    private void getThreadedBoundingBox(CallbackInfo ci, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        boundingBox.set(this.getOrCreateThreadedBoundingBox());
    }

    @Redirect(
            method = "setBoundingBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/StemBlock;boundingBoxMaxY:D",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedMaxY(StemBlock block, double value, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        boundingBox.get()[4] = value;
    }

    @Redirect(
            method = "setBoundingBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/StemBlock;boundingBoxMaxY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxY(StemBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[4];
    }
}
