package me.contaria.seedqueue.mixin.server.synchronization.biome.feature;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerBlock;
import net.minecraft.world.gen.feature.FlowerPatchFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FlowerPatchFeature.class)
public abstract class FlowerPatchFeatureMixin {
    @Unique
    private final ThreadLocal<FlowerBlock> threadedBlock = new ThreadLocal<>();
    @Unique
    private final ThreadLocal<BlockState> threadedState = new ThreadLocal<>();

    @Redirect(
            method = "setFlowers",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/FlowerPatchFeature;block:Lnet/minecraft/block/FlowerBlock;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedBlock(FlowerPatchFeature feature, FlowerBlock block) {
        this.threadedBlock.set(block);
    }

    @Redirect(
            method = "setFlowers",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/FlowerPatchFeature;state:Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedState(FlowerPatchFeature feature, BlockState state) {
        this.threadedState.set(state);
    }

    @Redirect(
            method = "generate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/FlowerPatchFeature;block:Lnet/minecraft/block/FlowerBlock;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private FlowerBlock getThreadedBlock(FlowerPatchFeature feature) {
        return this.threadedBlock.get();
    }

    @Redirect(
            method = "generate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/gen/feature/FlowerPatchFeature;state:Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BlockState getThreadedState(FlowerPatchFeature feature) {
        return this.threadedState.get();
    }
}
