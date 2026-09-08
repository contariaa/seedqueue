package me.contaria.seedqueue.mixin.server.synchronization.biome.top_base_block;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Biome.class)
public abstract class BiomeMixin implements SQBiome {
    @Shadow
    public BlockState topBlock;
    @Shadow
    public BlockState baseBlock;

    @Redirect(
            method = "method_8590",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/Biome;topBlock:Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BlockState getThreadedTopBlock(Biome biome) {
        return this.seedQueue$getTopBlock();
    }

    @Redirect(
            method = "method_8590",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/Biome;baseBlock:Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BlockState getThreadedBaseBlock(Biome biome) {
        return this.seedQueue$getBaseBlock();
    }

    @Override
    public BlockState seedQueue$getTopBlock() {
        return this.topBlock;
    }

    @Override
    public void seedQueue$setTopBlock(BlockState state) {
        throw new RuntimeException("Tried to set un-synchronized Biome#topBlock!");
    }

    @Override
    public BlockState seedQueue$getBaseBlock() {
        return this.baseBlock;
    }

    @Override
    public void seedQueue$setBaseBlock(BlockState state) {
        throw new RuntimeException("Tried to set un-synchronized Biome#baseBlock!");
    }
}
