package me.contaria.seedqueue.mixin.server.synchronization.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.LeavesBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin {
    @Unique
    private final ThreadLocal<int[]> threadedNeighborBlockDecayInfo = new ThreadLocal<>();

    @Redirect(
            method = "onScheduledTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/LeavesBlock;neighborBlockDecayInfo:[I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int[] getThreadedNeighborBlockDecayInfo(LeavesBlock block) {
        return this.threadedNeighborBlockDecayInfo.get();
    }

    @WrapOperation(
            method = "onScheduledTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/LeavesBlock;neighborBlockDecayInfo:[I",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedNeighborBlockDecayInfo(LeavesBlock block, int[] neighborBlockDecayInfo, Operation<Void> original) {
        this.threadedNeighborBlockDecayInfo.set(neighborBlockDecayInfo);
    }
}
