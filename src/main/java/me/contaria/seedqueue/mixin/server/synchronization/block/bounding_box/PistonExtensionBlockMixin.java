package me.contaria.seedqueue.mixin.server.synchronization.block.bounding_box;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.minecraft.block.PistonExtensionBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonExtensionBlock.class)
public abstract class PistonExtensionBlockMixin extends BlockMixin {

    @Redirect(
            method = "setBoundingBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/PistonExtensionBlock;boundingBoxMinX:D",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedMinX(PistonExtensionBlock block, double value, @Share("minX") LocalDoubleRef minX) {
        minX.set(value);
    }

    @Redirect(
            method = "setBoundingBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/PistonExtensionBlock;boundingBoxMinY:D",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedMinY(PistonExtensionBlock block, double value, @Share("minY") LocalDoubleRef minY) {
        minY.set(value);
    }

    @Redirect(
            method = "setBoundingBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/PistonExtensionBlock;boundingBoxMinZ:D",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedMinZ(PistonExtensionBlock block, double value, @Share("minZ") LocalDoubleRef minZ) {
        minZ.set(value);
    }

    @Redirect(
            method = "setBoundingBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/PistonExtensionBlock;boundingBoxMaxX:D",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedMaxX(PistonExtensionBlock block, double value, @Share("maxX") LocalDoubleRef maxX) {
        maxX.set(value);
    }

    @Redirect(
            method = "setBoundingBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/PistonExtensionBlock;boundingBoxMaxY:D",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedMaxY(PistonExtensionBlock block, double value, @Share("maxY") LocalDoubleRef maxY) {
        maxY.set(value);
    }

    @Redirect(
            method = "setBoundingBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/PistonExtensionBlock;boundingBoxMaxZ:D",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedMaxZ(PistonExtensionBlock block, double value, @Share("maxZ") LocalDoubleRef maxZ) {
        maxZ.set(value);
    }

    @Inject(
            method = "setBoundingBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/PistonExtensionBlock;boundingBoxMaxZ:D",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void setThreadedBoundingBox(CallbackInfo ci, @Share("minX") LocalDoubleRef minX, @Share("minY") LocalDoubleRef minY, @Share("minZ") LocalDoubleRef minZ, @Share("maxX") LocalDoubleRef maxX, @Share("maxY") LocalDoubleRef maxY, @Share("maxZ") LocalDoubleRef maxZ) {
        this.setThreadedBoundingBox(minX.get(), minY.get(), minZ.get(), maxX.get(), maxY.get(), maxZ.get());
    }
}
