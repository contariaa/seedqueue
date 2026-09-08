package me.contaria.seedqueue.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.BigTreeFeature;

public interface SQBiome {
    BlockState seedQueue$getTopBlock();

    void seedQueue$setTopBlock(BlockState state);

    BlockState seedQueue$getBaseBlock();

    void seedQueue$setBaseBlock(BlockState state);

    BigTreeFeature seedQueue$getBigTreeFeature();
}
