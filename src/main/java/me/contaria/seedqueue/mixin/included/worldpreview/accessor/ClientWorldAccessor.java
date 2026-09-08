package me.contaria.seedqueue.mixin.included.worldpreview.accessor;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.chunk.ClientChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientWorld.class)
public interface ClientWorldAccessor {
    @Accessor("clientChunkCache")
    ClientChunkProvider worldpreview$getClientChunkCache();

    @Invoker("tickBlocks")
    void worldpreview$tickBlocks();
}
