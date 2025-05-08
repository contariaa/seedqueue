package me.contaria.seedqueue.mixin.accessor;

import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Util.class)
public interface UtilAccessor {
    @Invoker("method_18347")
    static void seedQueue$uncaughtExceptionHandler(Thread thread, Throwable throwable) {
        throw new UnsupportedOperationException();
    }
}
