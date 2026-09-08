package me.contaria.seedqueue.mixin.server.synchronization.biome.top_base_block;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.ExtremeHillsBiome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExtremeHillsBiome.class)
public abstract class ExtremeHillsBiomeMixin extends BiomeMixin {
    @Unique
    private final ThreadLocal<BlockState> threadedTopBlock = ThreadLocal.withInitial(() -> this.topBlock);
    @Unique
    private final ThreadLocal<BlockState> threadedBaseBlock = ThreadLocal.withInitial(() -> this.baseBlock);

    // adding this constructor fixes a weird mixin bug
    // with merging the field initializers
    public ExtremeHillsBiomeMixin() {
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/ExtremeHillsBiome;topBlock:Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedTopBlock(ExtremeHillsBiome biome, BlockState topBlock) {
        ((SQBiome) biome).seedQueue$setTopBlock(topBlock);
    }

    @Redirect(
            method = "method_6420",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/ExtremeHillsBiome;baseBlock:Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedBaseBlock(ExtremeHillsBiome biome, BlockState baseBlock) {
        ((SQBiome) biome).seedQueue$setBaseBlock(baseBlock);
    }

    @Override
    public BlockState seedQueue$getTopBlock() {
        return this.threadedTopBlock.get();
    }

    @Override
    public void seedQueue$setTopBlock(BlockState state) {
        this.threadedTopBlock.set(state);
    }

    @Override
    public BlockState seedQueue$getBaseBlock() {
        return this.threadedBaseBlock.get();
    }

    @Override
    public void seedQueue$setBaseBlock(BlockState state) {
        this.threadedBaseBlock.set(state);
    }
}
