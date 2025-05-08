package me.contaria.seedqueue.mixin.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin extends MinecraftServerMixin {

    public IntegratedServerMixin(String string) {
        super(string);
    }

    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;getNetworkHandler()Lnet/minecraft/client/network/ClientPlayNetworkHandler;"
            )
    )
    private ClientPlayNetworkHandler doNotPauseBackgroundWorlds(ClientPlayNetworkHandler networkHandler) {
        if (this.seedQueue$inQueue()) {
            return null;
        }
        return networkHandler;
    }

    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;max(II)I"
            )
    )
    private int doNotChangeViewDistanceInQueue(int viewDistance) {
        if (this.seedQueue$inQueue()) {
            return this.getPlayerManager().getViewDistance();
        }
        return viewDistance;
    }

    @Inject(
            method = "loadWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/WorldGenerationProgressListenerFactory;create(I)Lnet/minecraft/server/WorldGenerationProgressListener;"
            )
    )
    private void setThreadLocalSeedQueueEntry(CallbackInfo ci) {
        this.seedQueue$getEntry().ifPresent(SeedQueue.LOCAL_ENTRY::set);
    }

    @WrapOperation(
            method = "loadWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldSaveHandler;readProperties()Lnet/minecraft/world/level/LevelProperties;"
            )
    )
    private LevelProperties doNotReadLevelPropertiesInQueue(WorldSaveHandler worldSaveHandler, Operation<LevelProperties> original) {
        if (this.seedQueue$inQueue()) {
            return null;
        }
        return original.call(worldSaveHandler);
    }
}
