package me.contaria.seedqueue.worldpreview;

import com.google.common.collect.Sets;
import me.contaria.seedqueue.mixin.included.worldpreview.accessor.ClientPlayNetworkHandlerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.LevelInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class WorldPreview {
    public static final Logger LOGGER = LogManager.getLogger();

    public static WorldRenderer worldRenderer;
    public static WorldPreviewProperties properties;

    public static boolean renderingPreview;

    public static WorldPreviewProperties configure(ServerWorld serverWorld, int perspective) {
        WPFakeServerPlayerEntity fakePlayer = new WPFakeServerPlayerEntity(
                serverWorld.getServer(),
                serverWorld,
                MinecraftClient.getInstance().getSession().getProfile(),
                new ServerPlayerInteractionManager(serverWorld)
        );

        ClientPlayNetworkHandler networkHandler = new ClientPlayNetworkHandler(
                MinecraftClient.getInstance(),
                null,
                null,
                MinecraftClient.getInstance().getSession().getProfile()
        );
        ClientPlayerInteractionManager interactionManager = new ClientPlayerInteractionManager(
                MinecraftClient.getInstance(),
                networkHandler
        );

        ClientWorld world = new ClientWorld(
                networkHandler,
                new LevelInfo(serverWorld.getLevelProperties()),
                serverWorld.dimension.getType(),
                serverWorld.getGlobalDifficulty(),
                MinecraftClient.getInstance().profiler
        );
        ClientPlayerEntity player = interactionManager.createPlayer(
                world,
                null
        );

        player.copyPosition(fakePlayer);
        // avoid the ClientPlayer being removed from previews on id collisions by setting its entityId
        // to the ServerPlayer's as would be done in the ClientPlayNetworkHandler
        player.setEntityId(fakePlayer.getEntityId());
        // copy the inventory from the server player, for mods like icarus to render given items on preview
        player.inventory.deserialize(fakePlayer.inventory.serialize(new NbtList()));
        player.inventory.selectedSlot = fakePlayer.inventory.selectedSlot;
        // reset the randomness introduced to the yaw in LivingEntity#<init>
        player.headYaw = player.yaw = 0.0F;

        LevelInfo.GameMode gameMode = LevelInfo.GameMode.NOT_SET;

        // This part is not actually relevant for previewing new worlds,
        // I just personally like the idea of worldpreview principally being able to work on old worlds as well
        // same with sending world info and scoreboard data
        NbtCompound playerData = serverWorld.getServer().getPlayerManager().getUserData();
        if (playerData != null) {
            player.fromNbt(playerData);
            // see ServerPlayerEntity#readCustomDataFromNbt
            if (!MinecraftServer.getServer().shouldForceGameMode() && playerData.contains("playerGameType", 99)) {
                gameMode = LevelInfo.GameMode.byId(playerData.getInt("playerGameType"));
            }
        }

        Queue<Packet<?>> packetQueue = new LinkedBlockingQueue<>();
        packetQueue.add(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, fakePlayer));
        packetQueue.add(new GameStateChangeS2CPacket(3, (gameMode != LevelInfo.GameMode.NOT_SET ? gameMode : serverWorld.getServer().getDefaultGameMode()).getId()));

        // see PlayerManager#sendWorldInfo
        packetQueue.add(new WorldBorderS2CPacket(serverWorld.getWorldBorder(), WorldBorderS2CPacket.Type.INITIALIZE));
        packetQueue.add(new WorldTimeUpdateS2CPacket(serverWorld.getLastUpdateTime(), serverWorld.getTimeOfDay(), serverWorld.getGameRules().getBoolean("doDaylightCycle")));
        packetQueue.add(new PlayerSpawnPositionS2CPacket(serverWorld.getSpawnPos()));
        if (serverWorld.isRaining()) {
            packetQueue.add(new GameStateChangeS2CPacket(1, 0.0F));
            packetQueue.add(new GameStateChangeS2CPacket(7, world.getRainGradient(1.0F)));
            packetQueue.add(new GameStateChangeS2CPacket(8, world.getThunderGradient(1.0F)));
        }

        // see PlayerManager#sendScoreboard
        ServerScoreboard scoreboard = (ServerScoreboard) serverWorld.getScoreboard();
        HashSet<ScoreboardObjective> set = Sets.newHashSet();
        for (Team team : scoreboard.getTeams()) {
            packetQueue.add(new TeamS2CPacket(team, 0));
        }
        for (int i = 0; i < 19; ++i) {
            ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(i);
            if (scoreboardObjective == null || set.contains(scoreboardObjective)) {
                continue;
            }
            for (Packet<?> packet : scoreboard.createChangePackets(scoreboardObjective)) {
                packetQueue.add(packet);
            }
            set.add(scoreboardObjective);
        }

        // make player model parts visible
        int playerModelPartsBitMask = 0;
        for (PlayerModelPart playerModelPart : MinecraftClient.getInstance().options.getEnabledPlayerModelParts()) {
            playerModelPartsBitMask |= playerModelPart.getBitFlag();
        }
        player.getDataTracker().setProperty(10, (byte) playerModelPartsBitMask);


        // set cape to player position
        player.capeX = player.prevCapeX = player.x;
        player.capeY = player.prevCapeY = player.y;
        player.capeZ = player.prevCapeZ = player.z;

        // set player chunk coordinates,
        // usually these get set when adding the entity to a chunk,
        // however the chunk the player is in is not actually loaded yet
        player.chunkX = MathHelper.floor(player.x / 16.0);
        player.chunkY = MathHelper.clamp(MathHelper.floor(player.y / 16.0), 0, 16);
        player.chunkZ = MathHelper.floor(player.z / 16.0);

        ((ClientPlayNetworkHandlerAccessor) player.networkHandler).worldpreview$setWorld(world);

        return new WorldPreviewProperties(world, player, interactionManager, packetQueue, perspective);
    }
}
