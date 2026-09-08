package me.contaria.seedqueue.mixin.included.worldpreview.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.contaria.seedqueue.worldpreview.WPFakeServerPlayerEntity;
import me.contaria.seedqueue.worldpreview.interfaces.WPMinecraftServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Shadow
    @Final
    public MinecraftServer server;

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getTopPosition(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;"
            ),
            index = 0
    )
    private BlockPos setPreviewSpawnPos(BlockPos pos, @Local(argsOnly = true) MinecraftServer minecraftServer) {
        WPMinecraftServer server = (WPMinecraftServer) minecraftServer;
        if (this.isWorldPreviewFakePlayer()) {
            return server.worldpreview$getPreviewSpawnPos();
        }
        BlockPos spawnPos = server.worldpreview$getPreviewSpawnPos();
        if (spawnPos != null) {
            server.worldpreview$clearPreviewSpawnPos();
            return spawnPos;
        }
        return pos;
    }

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;createStatHandler(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/stat/ServerStatHandler;"
            )
    )
    private ServerStatHandler doNotCreateStatHandler(PlayerManager playerManager, PlayerEntity player, Operation<ServerStatHandler> original) {
        if (this.isWorldPreviewFakePlayer()) {
            return null;
        }
        return original.call(playerManager, player);
    }

    @Unique
    private boolean isWorldPreviewFakePlayer() {
        return (Object) this instanceof WPFakeServerPlayerEntity;
    }
}
