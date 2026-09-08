package me.contaria.seedqueue.mixin.included.fastreset;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.contaria.seedqueue.fastreset.interfaces.FRMinecraftServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements FRMinecraftServer {
    @Shadow
    private volatile boolean loading;

    @Unique
    private volatile boolean fastReset;

    @WrapWithCondition(
            method = "stopServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;saveAllPlayerData()V"
            )
    )
    private boolean disablePlayerSaving(PlayerManager playerManager) {
        return this.fastReset$shouldSave();
    }

    @WrapWithCondition(
            method = "stopServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;saveWorlds(Z)V"
            )
    )
    private boolean disableSaving(MinecraftServer server, boolean silent) {
        return this.fastReset$shouldSave();
    }

    @Override
    public void fastReset$fastReset() {
        this.fastReset = true;
    }

    // MinecraftServer#loading actually means the complete opposite, more like "finishedLoading"
    // we check it to skip saving on WorldPreview resets
    @Override
    public boolean fastReset$shouldSave() {
        return !this.fastReset && this.loading;
    }
}
