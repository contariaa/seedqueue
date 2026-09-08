package me.contaria.seedqueue.mixin.server.synchronization.block.bounding_box;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.TripwireBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TripwireBlock.class)
public abstract class TripwireBlockMixin extends BlockMixin {
    @Inject(
            method = "updatePowered",
            at = @At("HEAD")
    )
    private void getThreadedBoundingBox(CallbackInfo ci, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        boundingBox.set(this.getThreadedBoundingBox());
    }

    @Redirect(
            method = "updatePowered",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/TripwireBlock;boundingBoxMinX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinX(TripwireBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[0];
    }

    @Redirect(
            method = "updatePowered",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/TripwireBlock;boundingBoxMinY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinY(TripwireBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[1];
    }

    @Redirect(
            method = "updatePowered",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/TripwireBlock;boundingBoxMinZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinZ(TripwireBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[2];
    }

    @Redirect(
            method = "updatePowered",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/TripwireBlock;boundingBoxMaxX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxX(TripwireBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[3];
    }

    @Redirect(
            method = "updatePowered",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/TripwireBlock;boundingBoxMaxY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxY(TripwireBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[4];
    }

    @Redirect(
            method = "updatePowered",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/TripwireBlock;boundingBoxMaxZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxZ(TripwireBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[5];
    }
}
