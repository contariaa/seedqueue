package me.contaria.seedqueue.mixin.accessor;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("currentThread")
    Thread seedQueue$getThread();

    @Invoker("handleProfilerKeyPress")
    void seedQueue$handleProfilerKeyPress(int digit);

    @Invoker("runGameLoop")
    void seedQueue$runGameLoop();
}
