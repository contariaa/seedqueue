package me.contaria.seedqueue.mixin.included.worldpreview.client.render;

import me.contaria.seedqueue.worldpreview.WorldPreview;
import net.minecraft.client.world.ChunkRenderCache;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChunkRenderCache.class)
public abstract class ChunkRenderCacheMixin {

    @ModifyArg(
            method = "getBlockEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;getBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/Chunk$Status;)Lnet/minecraft/block/entity/BlockEntity;"
            ),
            index = 1
    )
    private Chunk.Status immediatelyPopulateBlockEntitiesOnPreview(Chunk.Status status) {
        if (WorldPreview.renderingPreview) {
            return Chunk.Status.IMMEDIATE;
        }
        return status;
    }
}
