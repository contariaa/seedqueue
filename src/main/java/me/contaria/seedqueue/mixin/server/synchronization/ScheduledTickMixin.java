package me.contaria.seedqueue.mixin.server.synchronization;

import net.minecraft.util.ScheduledTick;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicLong;

// legacy crash fix also has this mixin,
// to avoid crashing we set a low priority and require = 0
@Mixin(value = ScheduledTick.class, priority = 500)
public abstract class ScheduledTickMixin {
    @Unique
    private static final AtomicLong atomicIdCounter = new AtomicLong();

    @Mutable
    @Shadow
    private long id;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/ScheduledTick;id:J",
                    opcode = Opcodes.PUTFIELD
            ),
            require = 0
    )
    private void atomicIdCounter(ScheduledTick tick, long id) {
        this.id = atomicIdCounter.incrementAndGet();
    }
}
