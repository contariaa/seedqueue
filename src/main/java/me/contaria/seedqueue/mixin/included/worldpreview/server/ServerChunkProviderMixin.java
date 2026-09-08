package me.contaria.seedqueue.mixin.included.worldpreview.server;

import me.contaria.seedqueue.SeedQueue;
import me.contaria.seedqueue.mixin.included.worldpreview.accessor.EntityTrackerAccessor;
import me.contaria.seedqueue.mixin.included.worldpreview.accessor.TrackedEntityInstanceAccessor;
import me.contaria.seedqueue.worldpreview.WorldPreviewProperties;
import me.contaria.seedqueue.worldpreview.interfaces.WPServerChunkProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TrackedEntityInstance;
import net.minecraft.entity.attribute.EntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilterableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ServerChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@Mixin(ServerChunkProvider.class)
public abstract class ServerChunkProviderMixin implements WPServerChunkProvider {
    @Shadow
    private ServerWorld world;
    @Shadow
    private List<Chunk> chunks;

    @Unique
    private final Set<Long> sentChunks = new HashSet<>();
    @Unique
    private final Set<Long> sentEmptyChunks = new HashSet<>();
    @Unique
    private final Set<Integer> sentEntities = new HashSet<>();

    @Unique
    private List<Packet<?>> processChunk(Chunk chunk) {
        ChunkPos pos = chunk.getChunkPos();
        if (this.sentChunks.contains(ChunkPos.getIdFromCoords(pos.x, pos.z))) {
            return Collections.emptyList();
        }

        List<Packet<?>> chunkPackets = new ArrayList<>();

        chunkPackets.add(new ChunkDataS2CPacket(chunk, true, 65535));
        //chunkPackets.add(new LightUpdateS2CPacket(chunk.getPos(), chunk.getLightingProvider()));
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            Packet<?> packet = blockEntity.getPacket();
            if (packet != null) {
                chunkPackets.add(packet);
            }
        }

        this.sentChunks.add(ChunkPos.getIdFromCoords(pos.x, pos.z));

        return chunkPackets;
    }

    @Unique
    private void sendData(Queue<Packet<?>> packetQueue, ClientPlayerEntity player, Chunk chunk) {
        ChunkPos pos = chunk.getChunkPos();
        ChunkPos playerPos = new ChunkPos(player.getBlockPos().getX() / 16, player.getBlockPos().getZ() / 16);
        if (Math.max(Math.abs(pos.x - playerPos.x), Math.abs(pos.z - playerPos.z)) > SeedQueue.config.previewChunkDistance) {
            return;
        }

        List<Packet<?>> chunkPackets = this.processChunk(chunk);

        List<Packet<?>> entityPackets = new ArrayList<>();
        for (TypeFilterableList<Entity> entities : chunk.getEntities()) {
            for (Entity entity : entities) {
                entityPackets.addAll(this.processEntity(entity));
            }
        }

        if (!entityPackets.isEmpty() && chunkPackets.isEmpty()) {
            if (!this.sentChunks.contains(ChunkPos.getIdFromCoords(pos.x, pos.z)) && this.sentEmptyChunks.add(ChunkPos.getIdFromCoords(pos.x, pos.z))) {
                chunkPackets = Collections.singletonList(this.createEmptyChunkPacket(chunk));
            }
        }

        packetQueue.addAll(chunkPackets);
        packetQueue.addAll(entityPackets);
    }

    @Unique
    private List<Packet<?>> processEntity(Entity entity) {
        int id = entity.getEntityId();
        if (this.sentEntities.contains(id)) {
            return Collections.emptyList();
        }

        // see TrackedEntityInstance#method_2184
        TrackedEntityInstance instance = ((EntityTrackerAccessor) this.world.getEntityTracker()).worldpreview$getTrackedEntityIds().get(id);
        TrackedEntityInstanceAccessor accessor = (TrackedEntityInstanceAccessor) instance;

        if (instance == null) {
            return Collections.emptyList();
        }

        List<Packet<?>> entityPackets = new ArrayList<>();

        Packet<?> spawnPacket = accessor.worldpreview$createSpawnPacket();
        entityPackets.add(spawnPacket);

        if (!entity.getDataTracker().isEmpty()) {
            entityPackets.add(new EntityTrackerUpdateS2CPacket(id, entity.getDataTracker(), true));
        }
        NbtCompound nbtCompound = entity.method_10948();
        if (nbtCompound != null) {
            entityPackets.add(new UpdateEntityNbtS2CPacket(id, nbtCompound));
        }
        if (entity instanceof LivingEntity) {
            EntityAttributeContainer entityAttributeContainer = (EntityAttributeContainer) ((LivingEntity) entity).getAttributeContainer();
            Collection<EntityAttributeInstance> collection = entityAttributeContainer.buildTrackedAttributesCollection();
            if (!collection.isEmpty()) {
                entityPackets.add(new EntityAttributesS2CPacket(id, collection));
            }
        }
        if (accessor.worldpreview$shouldTrackVelocity() && !(spawnPacket instanceof MobSpawnS2CPacket)) {
            entityPackets.add(new EntityVelocityUpdateS2CPacket(id, entity.velocityX, entity.velocityY, entity.velocityZ));
        }
        if (entity.vehicle != null) {
            entityPackets.add(new EntityAttachS2CPacket(0, entity, entity.vehicle));
        }
        if (entity instanceof MobEntity && ((MobEntity) entity).getLeashOwner() != null) {
            entityPackets.add(new EntityAttachS2CPacket(1, entity, ((MobEntity) entity).getLeashOwner()));
        }
        if (entity instanceof LivingEntity) {
            for (int i = 0; i < 5; i++) {
                ItemStack itemStack = ((LivingEntity) entity).getMainSlot(i);
                if (itemStack != null) {
                    entityPackets.add(new EntityEquipmentUpdateS2CPacket(id, i, itemStack));
                }
            }
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) entity;
            if (playerEntity.isSleeping()) {
                entityPackets.add(new BedSleepS2CPacket(playerEntity, new BlockPos(entity)));
            }
        }
        if (entity instanceof LivingEntity) {
            for (StatusEffectInstance statusEffectInstance : ((LivingEntity) entity).getStatusEffectInstances()) {
                entityPackets.add(new EntityStatusEffectS2CPacket(id, statusEffectInstance));
            }
        }

        entityPackets.add(new EntityS2CPacket.Rotate(id, (byte) MathHelper.floor(entity.yaw * 256.0f / 360.0f), (byte) MathHelper.floor(entity.pitch * 256.0f / 360.0f), entity.onGround));
        entityPackets.add(new EntitySetHeadYawS2CPacket(entity, (byte) MathHelper.floor(entity.getHeadRotation() * 256.0f / 360.0f)));

        this.sentEntities.add(id);
        return entityPackets;
    }

    @Unique
    private ChunkDataS2CPacket createEmptyChunkPacket(Chunk chunk) {
        Chunk empty = new Chunk(chunk.getWorld(), chunk.chunkX, chunk.chunkZ);
        empty.setBiomeArray(chunk.getBiomeArray());
        return new ChunkDataS2CPacket(empty, true, 65535);
    }

    @Override
    public void worldpreview$sendData(WorldPreviewProperties properties) {
        for (Chunk chunk : this.chunks) {
            this.sendData(properties.packetQueue, properties.player, chunk);
        }
    }
}
