package me.contaria.seedqueue.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.SeedQueueEntry;
import me.contaria.seedqueue.SeedQueueExecutorWrapper;
import me.contaria.seedqueue.compat.ModCompat;
import me.contaria.seedqueue.compat.SeedQueuePreviewProperties;
import me.contaria.seedqueue.debug.SeedQueueSystemInfo;
import me.contaria.seedqueue.gui.wall.SeedQueueWallScreen;
import me.contaria.seedqueue.interfaces.SQMinecraftServer;
import me.contaria.seedqueue.interfaces.SQSoundManager;
import me.contaria.seedqueue.interfaces.SQWorldGenerationProgressLogger;
import me.contaria.seedqueue.mixin.accessor.MinecraftServerAccessor;
import me.contaria.seedqueue.mixin.accessor.WorldGenerationProgressTrackerAccessor;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.UserCache;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.net.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(value = MinecraftClient.class, priority = 500)
public abstract class MinecraftClientMixin {

    @Shadow
    @Final
    private AtomicReference<WorldGenerationProgressTracker> worldGenProgressTracker;
    @Shadow
    @Nullable
    public Screen currentScreen;

    @Inject(
            method = "startIntegratedServer",
            at = @At("TAIL")
    )
    private void startSeedQueue(CallbackInfo ci) {
        // SeedQueue is started after one world has been created so AntiResourceReload is guaranteed to have populated its cache
        // this means we don't have to worry about synchronizing in that area
        if (Atum.isRunning() && !SeedQueue.isActive()) {
            SeedQueue.start();
        }
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldSaveHandler;readProperties()Lnet/minecraft/world/level/LevelProperties;"
            )
    )
    private LevelProperties doNotReadLevelProperties(WorldSaveHandler worldSaveHandler, Operation<LevelProperties> original) {
        if (SeedQueue.inQueue() || SeedQueue.currentEntry != null) {
            return null;
        }
        return original.call(worldSaveHandler);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/LevelStorage;createSaveHandler(Ljava/lang/String;Lnet/minecraft/server/MinecraftServer;)Lnet/minecraft/world/WorldSaveHandler;"
            )
    )
    private WorldSaveHandler loadWorldSaveHandler(LevelStorage storage, String name, MinecraftServer server, Operation<WorldSaveHandler> original) {
        if (!SeedQueue.inQueue() && SeedQueue.currentEntry != null) {
            return SeedQueue.currentEntry.getWorldSaveHandler();
        }
        return original.call(storage, name, server);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/LevelProperties;)Lnet/minecraft/world/level/LevelInfo;"
            )
    )
    private LevelInfo loadIntegratedResourceManager(LevelProperties properties, Operation<LevelInfo> original) {
        if (!SeedQueue.inQueue() && SeedQueue.currentEntry != null) {
            return SeedQueue.currentEntry.getLevelInfo();
        }
        return original.call(properties);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "NEW",
                    target = "(Ljava/net/Proxy;Ljava/lang/String;)Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;",
                    remap = false
            )
    )
    private YggdrasilAuthenticationService loadYggdrasilAuthenticationService(Proxy proxy, String clientToken, Operation<YggdrasilAuthenticationService> original) {
        if (!SeedQueue.inQueue() && SeedQueue.currentEntry != null) {
            YggdrasilAuthenticationService service = SeedQueue.currentEntry.getYggdrasilAuthenticationService();
            if (service != null) {
                return service;
            }
        }
        if (SeedQueue.inQueue() && SeedQueue.config.shouldUseWall()) {
            return null;
        }
        return original.call(proxy, clientToken);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;createMinecraftSessionService()Lcom/mojang/authlib/minecraft/MinecraftSessionService;",
                    remap = false
            )
    )
    private MinecraftSessionService loadMinecraftSessionService(YggdrasilAuthenticationService service, Operation<MinecraftSessionService> original) {
        if (!SeedQueue.inQueue() && SeedQueue.currentEntry != null) {
            MinecraftSessionService sessionService = SeedQueue.currentEntry.getMinecraftSessionService();
            if (sessionService != null) {
                return sessionService;
            }
        }
        if (SeedQueue.inQueue() && service == null) {
            return null;
        }
        return original.call(service);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;createProfileRepository()Lcom/mojang/authlib/GameProfileRepository;",
                    remap = false
            )
    )
    private GameProfileRepository loadGameProfileRepository(YggdrasilAuthenticationService service, Operation<GameProfileRepository> original) {
        if (!SeedQueue.inQueue() && SeedQueue.currentEntry != null) {
            GameProfileRepository repository = SeedQueue.currentEntry.getGameProfileRepository();
            if (repository != null) {
                return repository;
            }
        }
        if (SeedQueue.inQueue() && service == null) {
            return null;
        }
        return original.call(service);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "NEW",
                    target = "(Lcom/mojang/authlib/GameProfileRepository;Ljava/io/File;)Lnet/minecraft/util/UserCache;"
            )
    )
    private UserCache loadUserCache(GameProfileRepository profileRepository, File cacheFile, Operation<UserCache> original) {
        if (!SeedQueue.inQueue() && SeedQueue.currentEntry != null) {
            UserCache userCache = SeedQueue.currentEntry.getUserCache();
            if (userCache != null) {
                return userCache;
            }
        }
        if (SeedQueue.inQueue() && profileRepository == null) {
            // creating the UserCache is quite expensive compared to the rest of the server creation, so we do it lazily (see "loadServer")
            return null;
        }
        return original.call(profileRepository, cacheFile);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "NEW",
                    target = "Lnet/minecraft/server/integrated/IntegratedServer;"
            )
    )
    private IntegratedServer loadServer(MinecraftClient client, String levelName, String displayName, LevelInfo levelInfo, YggdrasilAuthenticationService authService, MinecraftSessionService sessionService, GameProfileRepository gameProfileRepo, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, Operation<IntegratedServer> original, @Local UserCache newUserCache, @Local MinecraftSessionService newSessionService, @Local GameProfileRepository newGameProfileRepo) {
        if (!SeedQueue.inQueue() && SeedQueue.currentEntry != null) {
            // see "loadUserCache"
            IntegratedServer server = SeedQueue.currentEntry.getServer();
            if (SeedQueue.currentEntry.getMinecraftSessionService() == null) {
                ((MinecraftServerAccessor) server).seedQueue$setSessionService(newSessionService);
            }
            if (SeedQueue.currentEntry.getGameProfileRepository() == null) {
                ((MinecraftServerAccessor) server).seedQueue$setGameProfileRepo(newGameProfileRepo);
            }
            if (SeedQueue.currentEntry.getUserCache() == null) {
                ((MinecraftServerAccessor) server).seedQueue$setUserCache(newUserCache);
            }
            server.getThread().setPriority(Thread.NORM_PRIORITY);
            return server;
        }
        return original.call(client, levelName, displayName, levelInfo, authService, sessionService, gameProfileRepo, userCache, worldGenerationProgressListenerFactory);
    }

    @WrapOperation(
            method = "startIntegratedServer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;server:Lnet/minecraft/server/integrated/IntegratedServer;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void queueServer(MinecraftClient client, IntegratedServer server, Operation<Void> original, @Local(argsOnly = true) LevelInfo levelInfo, @Local WorldSaveHandler worldSaveHandler, @Local YggdrasilAuthenticationService yggdrasilAuthenticationService, @Local MinecraftSessionService minecraftSessionService, @Local GameProfileRepository gameProfileRepository, @Local UserCache userCache) {
        if (SeedQueue.inQueue()) {
            ((SQMinecraftServer) server).seedQueue$setExecutor(SeedQueueExecutorWrapper.SEEDQUEUE_EXECUTOR);
            SeedQueue.add(new SeedQueueEntry(server, levelInfo, worldSaveHandler, yggdrasilAuthenticationService, minecraftSessionService, gameProfileRepository, userCache));
            server.start();
            return;
        }
        original.call(client, server);
        if (SeedQueue.currentEntry != null) {
            ((SQMinecraftServer) server).seedQueue$resetExecutor();
            SeedQueue.currentEntry.load();
        }
    }

    @WrapWithCondition(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/integrated/IntegratedServer;start()V"
            )
    )
    private boolean doNotStartServerTwice(IntegratedServer server) {
        return SeedQueue.currentEntry == null;
    }

    @WrapOperation(
            method = "method_17533",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/atomic/AtomicReference;set(Ljava/lang/Object;)V",
                    remap = false
            )
    )
    private void saveWorldGenerationProgressTracker(AtomicReference<?> instance, Object tracker, Operation<Void> original) {
        Optional<SeedQueueEntry> entry = SeedQueue.getThreadLocalEntry();
        if (entry.isPresent()) {
            ((SQWorldGenerationProgressLogger) ((WorldGenerationProgressTrackerAccessor) tracker).getProgressLogger()).seedQueue$mute();
            entry.get().setWorldGenerationProgressTracker((WorldGenerationProgressTracker) tracker);
            return;
        }
        original.call(instance, tracker);
    }

    @Inject(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/atomic/AtomicReference;get()Ljava/lang/Object;",
                    ordinal = 0,
                    remap = false
            )
    )
    private void loadWorldGenerationProgressTracker(CallbackInfo ci) {
        if (!SeedQueue.inQueue() && SeedQueue.currentEntry != null) {
            WorldGenerationProgressTracker tracker = SeedQueue.currentEntry.getWorldGenerationProgressTracker();
            // tracker could be null if the SeedQueueEntry is loaded before the server creates the tracker,
            // in that case the vanilla logic will loop and wait for the tracker to be created
            if (tracker != null) {
                ((SQWorldGenerationProgressLogger) ((WorldGenerationProgressTrackerAccessor) tracker).getProgressLogger()).seedQueue$unmute();
                this.worldGenProgressTracker.set(tracker);
            }
        }
    }

    @Inject(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/integrated/IntegratedServer;start()V"
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
                    target = "Lnet/minecraft/client/MinecraftClient;disconnect()V"
            )
    )
    private boolean cancelDisconnect(MinecraftClient client) {
        return !SeedQueue.inQueue();
    }

    @WrapWithCondition(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/atomic/AtomicReference;set(Ljava/lang/Object;)V",
                    remap = false
            )
    )
    private boolean cancelWorldGenTrackerSetNull(AtomicReference<?> instance, Object value) {
        return !SeedQueue.inQueue();
    }

    @WrapWithCondition(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/SkullBlockEntity;setUserCache(Lnet/minecraft/util/UserCache;)V"
            )
    )
    private boolean cancelSetUserCache(UserCache value) {
        return !SeedQueue.inQueue();
    }

    @WrapWithCondition(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/SkullBlockEntity;setSessionService(Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V"
            )
    )
    private boolean cancelSetSessionService(MinecraftSessionService value) {
        return !SeedQueue.inQueue();
    }

    @WrapWithCondition(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/UserCache;setUseRemote(Z)V"
            )
    )
    private boolean cancelSetUseRemote(boolean value) {
        return !SeedQueue.inQueue();
    }

    @WrapWithCondition(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V",
                    ordinal = 0
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/screen/LevelLoadingScreen;<init>(Lnet/minecraft/client/gui/WorldGenerationProgressTracker;)V"
                    )
            )
    )
    private boolean smoothTransition(MinecraftClient client, Screen screen) {
        if (SeedQueue.inQueue()) {
            return false;
        }
        if (SeedQueue.currentEntry == null) {
            return true;
        }
        return !SeedQueue.currentEntry.isReady();
    }

    @Inject(
            method = "startIntegratedServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;render(Z)V"
            )
    )
    private void loadPreviewProperties(CallbackInfo ci) {
        if (ModCompat.worldpreview$inPreview() || SeedQueue.currentEntry == null) {
            return;
        }
        SeedQueuePreviewProperties previewProperties = SeedQueue.currentEntry.getPreviewProperties();
        if (previewProperties == null) {
            return;
        }
        // player model configuration is suppressed in WorldPreviewMixin#doNotSetPlayerModelParts_inQueue for SeedQueue worlds
        // when using wall, player model will be configured in SeedQueueEntry#setSettingsCache
        if (SeedQueue.currentEntry.getSettingsCache() == null) {
            previewProperties.loadPlayerModelParts();
        }
        previewProperties.load();
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
            method = "openScreen",
            at = @At("RETURN")
    )
    private void pingSeedQueueThreadOnOpeningWall(Screen screen, CallbackInfo ci) {
        if (screen instanceof SeedQueueWallScreen) {
            SeedQueue.ping();
        }
    }

    @WrapWithCondition(
            method = {
                    "reset",
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;render(Z)V"
            )
    )
    private boolean skipIntermissionScreens(MinecraftClient instance, boolean tick) {
        return !SeedQueue.isActive();
    }

    @WrapOperation(
            method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/integrated/IntegratedServer;isStopping()Z"
            )
    )
    private boolean fastQuit(IntegratedServer server, Operation<Boolean> original) {
        return original.call(server) || (SeedQueue.isActive() && !ModCompat.fastReset$shouldSave(server));
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void logSystemInformation(CallbackInfo ci) {
        SeedQueueSystemInfo.logSystemInformation();
    }

    @Inject(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;render(Z)V"
            )
    )
    private void runClientTasks(CallbackInfo ci) {
        SeedQueue.runClientThreadTasks();
    }

    @Inject(
            method = "getFramerateLimit",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifyFPSOnWall(CallbackInfoReturnable<Integer> cir) {
        if (SeedQueue.isOnWall()) {
            cir.setReturnValue(SeedQueue.config.wallFPS);
        }
    }

    @ModifyExpressionValue(
            method = "render",
            at = {
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/options/GameOptions;debugEnabled:Z"
                    ),
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/options/GameOptions;debugProfilerEnabled:Z"
                    )
            }
    )
    private boolean showDebugMenuOnWall(boolean enabled) {
        return enabled || (SeedQueue.isOnWall() && SeedQueue.config.showDebugMenu);
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/options/GameOptions;hudHidden:Z"
            )
    )
    private boolean showDebugMenuOnWall2(boolean hudHidden) {
        return hudHidden && !(SeedQueue.isOnWall() && SeedQueue.config.showDebugMenu);
    }

    @WrapWithCondition(
            method = "render",
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
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/Window;swapBuffers()V",
                    shift = At.Shift.AFTER
            )
    )
    private void finishRenderingWall(CallbackInfo ci) {
        if (this.currentScreen instanceof SeedQueueWallScreen) {
            SeedQueueWallScreen wall = (SeedQueueWallScreen) this.currentScreen;
            wall.joinScheduledInstance();
            wall.populateResetCooldowns();
            wall.tickBenchmark();
        }
    }

    @WrapWithCondition(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sound/MusicTracker;tick()V"
            )
    )
    private boolean doNotPlayMusicOnWall(MusicTracker musicTracker) {
        return !SeedQueue.isOnWall();
    }

    @WrapOperation(
            method = "reset",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sound/SoundManager;stopAll()V"
            )
    )
    private void keepSeedQueueSounds(SoundManager soundManager, Operation<Void> original) {
        if (SeedQueue.isActive()) {
            ((SQSoundManager) soundManager).seedQueue$stopAllExceptSeedQueueSounds();
            return;
        }
        original.call(soundManager);
    }

    @Inject(
            method = "stop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;disconnect()V",
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
