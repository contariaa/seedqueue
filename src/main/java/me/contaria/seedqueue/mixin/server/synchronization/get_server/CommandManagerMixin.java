package me.contaria.seedqueue.mixin.server.synchronization.get_server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getServer()Lnet/minecraft/server/MinecraftServer;"
            )
    )
    private MinecraftServer skipDedicatedServerCheck() {
        return null;
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;isDedicated()Z"
            )
    )
    private boolean skipDedicatedServerCheck(MinecraftServer server) {
        return false;
    }
}
