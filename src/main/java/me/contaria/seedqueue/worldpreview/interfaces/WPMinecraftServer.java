package me.contaria.seedqueue.worldpreview.interfaces;

import net.minecraft.util.math.BlockPos;

public interface WPMinecraftServer {
    void worldpreview$setPreviewSpawnPos(BlockPos pos);

    BlockPos worldpreview$getPreviewSpawnPos();

    void worldpreview$clearPreviewSpawnPos();
}
