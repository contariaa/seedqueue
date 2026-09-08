package me.contaria.seedqueue.mixin.server.synchronization.block;

import net.minecraft.block.FurnaceBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FurnaceBlock.class)
public abstract class FurnaceBlockMixin {
    @Unique
    private static final ThreadLocal<Boolean> threadedKeepInventory = ThreadLocal.withInitial(() -> false);

    @Redirect(
            method = "setBlockState",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/FurnaceBlock;keepInventory:Z",
                    opcode = Opcodes.PUTSTATIC
            )
    )
    private static void setThreadedKeepInventory(boolean keepInventory) {
        threadedKeepInventory.set(keepInventory);
    }

    @Redirect(
            method = "onBreaking",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/FurnaceBlock;keepInventory:Z",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static boolean getThreadedKeepInventory() {
        return threadedKeepInventory.get();
    }
}
