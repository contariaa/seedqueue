package me.contaria.seedqueue.mixin.server.synchronization.block.falling_block;

import me.contaria.seedqueue.synchronization.ThreadedFallingBlock;
import net.minecraft.block.DragonEggBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DragonEggBlock.class)
public abstract class DragonEggBlockMixin {

    @Redirect(
            method = "scheduledTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/FallingBlock;instantFall:Z",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private boolean getThreadedInstantFall() {
        return ThreadedFallingBlock.instantFall.get();
    }
}
