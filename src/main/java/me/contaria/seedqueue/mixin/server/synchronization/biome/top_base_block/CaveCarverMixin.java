package me.contaria.seedqueue.mixin.server.synchronization.biome.top_base_block;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.carver.CaveCarver;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CaveCarver.class)
public abstract class CaveCarverMixin {

    @Redirect(
            method = "carveCave(JIILnet/minecraft/world/chunk/ChunkBlockStateStorage;DDDFFFIID)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/Biome;topBlock:Lnet/minecraft/block/BlockState;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BlockState getThreadedTopBlock(Biome biome) {
        return ((SQBiome) biome).seedQueue$getTopBlock();
    }
}
