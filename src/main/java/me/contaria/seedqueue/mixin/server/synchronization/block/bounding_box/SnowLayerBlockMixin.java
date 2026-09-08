package me.contaria.seedqueue.mixin.server.synchronization.block.bounding_box;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.SnowLayerBlock;
import net.minecraft.util.math.Box;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SnowLayerBlock.class)
public abstract class SnowLayerBlockMixin extends BlockMixin {
    @Inject(
            method = "getCollisionBox",
            at = @At("HEAD")
    )
    private void getThreadedBoundingBox(CallbackInfoReturnable<Box> cir, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        boundingBox.set(this.getThreadedBoundingBox());
    }

    @Redirect(
            method = "getCollisionBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/SnowLayerBlock;boundingBoxMinX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinX(SnowLayerBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[0];
    }

    @Redirect(
            method = "getCollisionBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/SnowLayerBlock;boundingBoxMinY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinY(SnowLayerBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[1];
    }

    @Redirect(
            method = "getCollisionBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/SnowLayerBlock;boundingBoxMinZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinZ(SnowLayerBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[2];
    }

    @Redirect(
            method = "getCollisionBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/SnowLayerBlock;boundingBoxMaxX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxX(SnowLayerBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[3];
    }

    @Redirect(
            method = "getCollisionBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/SnowLayerBlock;boundingBoxMaxZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxZ(SnowLayerBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[5];
    }
}
