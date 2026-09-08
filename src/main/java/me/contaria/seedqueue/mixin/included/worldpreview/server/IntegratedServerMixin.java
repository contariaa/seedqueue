package me.contaria.seedqueue.mixin.included.worldpreview.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.SeedQueueEntry;
import me.contaria.seedqueue.interfaces.SQMinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.security.KeyPair;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin implements SQMinecraftServer {

    @WrapOperation(
            method = "setupServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkEncryptionUtils;generateServerKeyPair()Ljava/security/KeyPair;"
            )
    )
    private KeyPair skipGeneratingKeyPairOnFakePreview(Operation<KeyPair> original) {
        if (!SeedQueue.config.generateFakePreview) {
            return original.call();
        }
        SeedQueueEntry entry = this.seedQueue$getEntry();
        if (entry != null && !entry.isLocked()) {
            return null;
        }
        return original.call();
    }
}
