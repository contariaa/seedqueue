package me.contaria.seedqueue.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.SeedQueueEntry;
import me.contaria.seedqueue.debug.SeedQueueSystemInfo;
import me.contaria.seedqueue.gui.wall.SeedQueueWallScreen;
import me.contaria.seedqueue.mixin.accessor.MinecraftServerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.SaveHandler;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorageAccess;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @WrapWithCondition(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;connect(Lnet/minecraft/client/world/ClientWorld;)V"
            )
    )
    private boolean doNotDisconnectInQueue(MinecraftClient instance, ClientWorld world) {
        return !SeedQueue.inQueue();
    }

    @WrapWithCondition(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/System;gc()V",
                    remap = false
            )
    )
    private boolean doNotGcInQueue() {
        return !SeedQueue.inQueue() && SeedQueue.currentEntry == null;
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/LevelStorageAccess;createSaveHandler(Ljava/lang/String;Z)Lnet/minecraft/world/SaveHandler;"
            )
    )
    private SaveHandler loadSaveHandler(LevelStorageAccess storage, String worldName, boolean createPlayerDataDir, Operation<SaveHandler> original, @Share("saveHandler") LocalRef<SaveHandler> saveHandler) {
        if (SeedQueue.inQueue()) {
            saveHandler.set(original.call(storage, worldName, createPlayerDataDir));
            return saveHandler.get();
        }
        if (SeedQueue.currentEntry != null) {
            return SeedQueue.currentEntry.getSaveHandler();
        }
        return original.call(storage, worldName, createPlayerDataDir);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/LevelInfo;Ljava/lang/String;)Lnet/minecraft/world/level/LevelProperties;"
            )
    )
    private LevelProperties saveLevelProperties(LevelInfo levelInfo, String worldName, Operation<LevelProperties> original, @Share("levelProperties") LocalRef<LevelProperties> levelProperties) {
        if (SeedQueue.inQueue()) {
            levelProperties.set(original.call(levelInfo, worldName));
            return levelProperties.get();
        }
        if (SeedQueue.currentEntry != null) {
            return SeedQueue.currentEntry.getLevelProperties();
        }
        return original.call(levelInfo, worldName);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/SaveHandler;getLevelProperties()Lnet/minecraft/world/level/LevelProperties;"
            )
    )
    private LevelProperties doNotReadLevelProperties(SaveHandler saveHandler, Operation<LevelProperties> original) {
        if (SeedQueue.inQueue() || SeedQueue.currentEntry != null) {
            return null;
        }
        return original.call(saveHandler);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "NEW",
                    target = "Lnet/minecraft/server/integrated/IntegratedServer;"
            )
    )
    private IntegratedServer loadServer(MinecraftClient client, String worldName, String levelName, LevelInfo levelInfo, Operation<IntegratedServer> original) {
        if (!SeedQueue.inQueue() && SeedQueue.currentEntry != null) {
            IntegratedServer server = SeedQueue.currentEntry.getServer();
            ((MinecraftServerAccessor) server).seedQueue$getServerThread().setPriority(Thread.NORM_PRIORITY);
            return server;
        }
        return original.call(client, worldName, levelName, levelInfo);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;server:Lnet/minecraft/server/integrated/IntegratedServer;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void queueServer(MinecraftClient client, IntegratedServer server, Operation<Void> original, @Local(argsOnly = true) LevelInfo levelInfo, @Share("saveHandler") LocalRef<SaveHandler> saveHandler, @Share("levelProperties") LocalRef<LevelProperties> levelProperties) {
        if (SeedQueue.inQueue()) {
            SeedQueue.add(new SeedQueueEntry(server, saveHandler.get(), levelProperties.get(), levelInfo));
            server.startServerThread();
            return;
        }
        original.call(client, server);
        if (SeedQueue.currentEntry != null) {
            SeedQueue.currentEntry.load();
        }
    }

    @Inject(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/integrated/IntegratedServer;startServerThread()V"
            ),
            cancellable = true
    )
    private void cancelJoiningWorld(CallbackInfo ci) {
        if (SeedQueue.inQueue()) {
            ci.cancel();
        }
    }

    @WrapWithCondition(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/integrated/IntegratedServer;startServerThread()V"
            )
    )
    private boolean doNotStartServerTwice(IntegratedServer server) {
        return SeedQueue.currentEntry == null;
    }

    @Inject(
            method = "startIntegratedServer",
            at = @At("TAIL")
    )
    private void pingSeedQueueThreadOnLoadingWorld(CallbackInfo ci) {
        if (!SeedQueue.inQueue()) {
            SeedQueue.ping();
        }
    }

    @Inject(
            method = "setScreen",
            at = @At("RETURN")
    )
    private void pingSeedQueueThreadOnOpeningWall(Screen screen, CallbackInfo ci) {
        if (screen instanceof SeedQueueWallScreen) {
            SeedQueue.ping();
        }
    }

    @ModifyExpressionValue(
            method = "runGameLoop",
            at = {
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/option/GameOptions;debugEnabled:Z",
                            opcode = Opcodes.GETFIELD
                    ),
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/option/GameOptions;debugProfilerEnabled:Z",
                            opcode = Opcodes.GETFIELD
                    )
            }
    )
    private boolean showDebugMenuOnWall(boolean enabled) {
        return enabled || (SeedQueue.isOnWall() && SeedQueue.config.showDebugMenu);
    }

    @ModifyExpressionValue(
            method = "runGameLoop",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/option/GameOptions;hudHidden:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean showDebugMenuOnWall2(boolean hudHidden) {
        return hudHidden && !(SeedQueue.isOnWall() && SeedQueue.config.showDebugMenu);
    }

    @WrapWithCondition(
            method = "runGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Thread;yield()V"
            )
    )
    private boolean doNotYieldRenderThreadOnWall() {
        // because of the increased amount of threads when using SeedQueue,
        // not yielding the render thread results in a much smoother experience on the Wall Screen
        return !SeedQueue.isOnWall();
    }

    @Inject(
            method = "runGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;updateDisplay()V",
                    shift = At.Shift.AFTER
            )
    )
    private void finishRenderingWall(CallbackInfo ci) {
        if (SeedQueue.isOnWall()) {
            SeedQueueWallScreen wall = (SeedQueueWallScreen) MinecraftClient.getInstance().currentScreen;
            wall.joinScheduledInstance();
            wall.populateResetCooldowns();
            wall.tickBenchmark();
        }
    }

    @Inject(
            method = "getMaxFramerate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifyFPSOnWall(CallbackInfoReturnable<Integer> cir) {
        if (SeedQueue.isOnWall()) {
            cir.setReturnValue(SeedQueue.config.wallFPS);
        }
    }

    @Inject(
            method = "initializeGame",
            at = @At("TAIL")
    )
    private void logSystemInformation(CallbackInfo ci) {
        SeedQueueSystemInfo.logSystemInformation();
    }

    @Inject(
            method = "stop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;connect(Lnet/minecraft/client/world/ClientWorld;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void shutdownQueue(CallbackInfo ci) {
        SeedQueue.stop();
    }

    @Inject(
            method = "printCrashReport",
            at = @At("HEAD")
    )
    private static void shutdownQueueOnCrash(CallbackInfo ci) {
        // don't try to stop SeedQueue if Minecraft crashes before the client is initialized
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().isOnThread()) {
            SeedQueue.stop();
        }
    }
}
