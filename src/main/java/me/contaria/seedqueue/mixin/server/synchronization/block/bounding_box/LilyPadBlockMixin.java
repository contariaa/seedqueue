package me.contaria.seedqueue.mixin.server.synchronization.block.bounding_box;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.util.math.Box;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LilyPadBlock.class)
public abstract class LilyPadBlockMixin extends BlockMixin {
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
                    target = "Lnet/minecraft/block/LilyPadBlock;boundingBoxMinX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinX(LilyPadBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[0];
    }

    @Redirect(
            method = "getCollisionBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/LilyPadBlock;boundingBoxMinY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinY(LilyPadBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[1];
    }

    @Redirect(
            method = "getCollisionBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/LilyPadBlock;boundingBoxMinZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinZ(LilyPadBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[2];
    }

    @Redirect(
            method = "getCollisionBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/LilyPadBlock;boundingBoxMaxX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxX(LilyPadBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[3];
    }

    @Redirect(
            method = "getCollisionBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/LilyPadBlock;boundingBoxMaxY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxY(LilyPadBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[4];
    }

    @Redirect(
            method = "getCollisionBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/LilyPadBlock;boundingBoxMaxZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxZ(LilyPadBlock block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[5];
    }
}
