package me.contaria.seedqueue.mixin.server.synchronization.block.falling_block;

import me.contaria.seedqueue.synchronization.ThreadedFallingBlock;
import net.minecraft.world.chunk.EndChunkGenerator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndChunkGenerator.class)
public abstract class EndChunkGeneratorMixin {

    @Redirect(
            method = "decorateChunk",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/FallingBlock;instantFall:Z",
                    opcode = Opcodes.PUTSTATIC
            )
    )
    private void setThreadedInstantFall(boolean instantFall) {
        ThreadedFallingBlock.instantFall.set(instantFall);
    }
}
