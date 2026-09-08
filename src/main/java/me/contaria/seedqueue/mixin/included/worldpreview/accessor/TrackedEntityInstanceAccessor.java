package me.contaria.seedqueue.mixin.included.worldpreview.accessor;

import net.minecraft.entity.TrackedEntityInstance;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TrackedEntityInstance.class)
public interface TrackedEntityInstanceAccessor {
    @Accessor("trackVelocity")
    boolean worldpreview$shouldTrackVelocity();

    @Invoker("method_2182")
    Packet<?> worldpreview$createSpawnPacket();
}
