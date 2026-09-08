package me.contaria.seedqueue.mixin.accessor;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("running")
    void seedQueue$setRunning(boolean running);

    @Accessor("serverThread")
    Thread seedQueue$getServerThread();
}
