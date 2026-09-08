package me.contaria.seedqueue.mixin.included.antiresourcereload;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.io.File;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Unique
    private static UserCache cachedUserCache;

    @WrapOperation(
            method = {
                    "<init>(Ljava/net/Proxy;Ljava/io/File;)V",
                    "<init>(Ljava/io/File;Ljava/net/Proxy;Ljava/io/File;)V"
            },
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/server/MinecraftServer;Ljava/io/File;)Lnet/minecraft/util/UserCache;"
            )
    )
    private UserCache cacheUserCache(MinecraftServer server, File file, Operation<UserCache> original) {
        if (cachedUserCache == null) {
            cachedUserCache = original.call(null, file);
        }
        return cachedUserCache;
    }
}
