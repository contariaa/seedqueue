package me.contaria.seedqueue.mixin.server.synchronization.biome.decorator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.EndBiomeDecorator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(EndBiomeDecorator.class)
public abstract class EndBiomeDecoratorMixin extends BiomeDecoratorMixin {

    @Redirect(
            method = "generate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/EndBiomeDecorator;world:Lnet/minecraft/world/World;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private World getThreadedWorld(EndBiomeDecorator decorator) {
        return this.threadedWorld.get();
    }

    @Redirect(
            method = "generate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/EndBiomeDecorator;random:Ljava/util/Random;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Random getThreadedRandom(EndBiomeDecorator decorator) {
        return this.threadedRandom.get();
    }

    @Redirect(
            method = "generate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/EndBiomeDecorator;startPos:Lnet/minecraft/util/math/BlockPos;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BlockPos getThreadedStartPos(EndBiomeDecorator decorator) {
        return this.threadedStartPos.get();
    }
}
