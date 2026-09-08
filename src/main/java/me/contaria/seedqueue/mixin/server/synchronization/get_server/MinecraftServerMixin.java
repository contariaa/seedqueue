package me.contaria.seedqueue.mixin.server.synchronization.get_server;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Unique
    private static final ThreadLocal<MinecraftServer> threadedInstance = new ThreadLocal<>();

    @Inject(
            method = "run",
            at = @At("HEAD")
    )
    private void setThreadLocalInstance(CallbackInfo ci) {
        threadedInstance.set((MinecraftServer) (Object) this);
    }

    @Redirect(
            method = "<init>*",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/MinecraftServer;instance:Lnet/minecraft/server/MinecraftServer;",
                    opcode = Opcodes.PUTSTATIC
            )
    )
    private void noStaticServerInstance(MinecraftServer server) {
    }

    /**
     * @author contaria
     * @reason Since SeedQueue can load multiple servers at the same time, a static instance doesn't work
     */
    @Overwrite
    public static MinecraftServer getServer() {
        if (MinecraftClient.getInstance().isOnThread()) {
            return MinecraftClient.getInstance().getServer();
        }
        return Objects.requireNonNull(threadedInstance.get());
    }
}
