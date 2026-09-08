package me.contaria.seedqueue.mixin.included.antiresourcereload;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.UserCache;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(UserCache.class)
public abstract class UserCacheMixin {

    @ModifyExpressionValue(
            method = "findByName",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/UserCache;server:Lnet/minecraft/server/MinecraftServer;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private MinecraftServer useCurrentServer(MinecraftServer server) {
        if (server == null) {
            return MinecraftServer.getServer();
        }
        return server;
    }
}
