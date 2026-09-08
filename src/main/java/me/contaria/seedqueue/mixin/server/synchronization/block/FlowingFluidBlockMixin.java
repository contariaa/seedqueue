package me.contaria.seedqueue.mixin.server.synchronization.block;

import net.minecraft.block.FlowingFluidBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FlowingFluidBlock.class)
public abstract class FlowingFluidBlockMixin {
    @Unique
    private final ThreadLocal<Integer> threadedNeighbourSourceBlocks = ThreadLocal.withInitial(() -> 0);

    @Redirect(
            method = {
                    "onScheduledTick",
                    "getFluidLevelFromNeighbor"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/FlowingFluidBlock;neighborSourceBlocks:I",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedNeighbourSourceBlocks(FlowingFluidBlock block, int neighborSourceBlocks) {
        this.threadedNeighbourSourceBlocks.set(neighborSourceBlocks);
    }

    @Redirect(
            method = {
                    "onScheduledTick",
                    "getFluidLevelFromNeighbor"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/FlowingFluidBlock;neighborSourceBlocks:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int getThreadedNeighbourSourceBlocks(FlowingFluidBlock block) {
        return this.threadedNeighbourSourceBlocks.get();
    }
}
