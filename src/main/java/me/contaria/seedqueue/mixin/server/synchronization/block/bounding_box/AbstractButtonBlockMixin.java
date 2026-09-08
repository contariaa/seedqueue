package me.contaria.seedqueue.mixin.server.synchronization.block.bounding_box;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.AbstractButtonBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButtonBlock.class)
public abstract class AbstractButtonBlockMixin extends BlockMixin {
    @Inject(
            method = "onPossibleArrowCollision",
            at = @At("HEAD")
    )
    private void getThreadedBoundingBox(CallbackInfo ci, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        boundingBox.set(this.getThreadedBoundingBox());
    }

    @Redirect(
            method = "onPossibleArrowCollision",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/AbstractButtonBlock;boundingBoxMinX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinX(AbstractButtonBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[0];
    }

    @Redirect(
            method = "onPossibleArrowCollision",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/AbstractButtonBlock;boundingBoxMinY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinY(AbstractButtonBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[1];
    }

    @Redirect(
            method = "onPossibleArrowCollision",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/AbstractButtonBlock;boundingBoxMinZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinZ(AbstractButtonBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[2];
    }

    @Redirect(
            method = "onPossibleArrowCollision",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/AbstractButtonBlock;boundingBoxMaxX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxX(AbstractButtonBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[3];
    }

    @Redirect(
            method = "onPossibleArrowCollision",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/AbstractButtonBlock;boundingBoxMaxY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxY(AbstractButtonBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[4];
    }

    @Redirect(
            method = "onPossibleArrowCollision",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/AbstractButtonBlock;boundingBoxMaxZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxZ(AbstractButtonBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[5];
    }
}
