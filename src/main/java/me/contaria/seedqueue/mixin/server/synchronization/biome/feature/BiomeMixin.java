package me.contaria.seedqueue.mixin.server.synchronization.biome.feature;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.BigTreeFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Biome.class)
public abstract class BiomeMixin implements SQBiome {
    @Unique
    private final ThreadLocal<BigTreeFeature> threadedBigTreeFeature = ThreadLocal.withInitial(() -> new BigTreeFeature(false));

    @Redirect(
            method = "method_3822",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/Biome;field_4631:Lnet/minecraft/world/gen/feature/BigTreeFeature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BigTreeFeature createBigTreeFeature(Biome biome) {
        return this.seedQueue$getBigTreeFeature();
    }

    @Override
    public BigTreeFeature seedQueue$getBigTreeFeature() {
        return this.threadedBigTreeFeature.get();
    }
}
