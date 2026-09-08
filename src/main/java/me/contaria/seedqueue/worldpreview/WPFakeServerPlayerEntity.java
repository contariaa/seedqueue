package me.contaria.seedqueue.worldpreview;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;

public class WPFakeServerPlayerEntity extends ServerPlayerEntity {
    public WPFakeServerPlayerEntity(MinecraftServer server, ServerWorld world, GameProfile profile, ServerPlayerInteractionManager interactionManager) {
        super(server, world, profile, interactionManager);
    }
}
