package me.contaria.seedqueue.mixin.server.synchronization.block.bounding_box;

import net.minecraft.block.WallBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WallBlock.class)
public abstract class WallBlockMixin extends BlockMixin {

    @Redirect(
            method = "getCollisionBox",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/WallBlock;boundingBoxMaxY:D",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedMaxY(WallBlock block, double value) {
        this.getOrCreateThreadedBoundingBox()[4] = value;
    }
}
