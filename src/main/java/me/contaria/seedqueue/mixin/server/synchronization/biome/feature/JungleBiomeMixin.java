package me.contaria.seedqueue.mixin.server.synchronization.biome.feature;

import me.contaria.seedqueue.interfaces.SQBiome;
import net.minecraft.world.biome.JungleBiome;
import net.minecraft.world.gen.feature.BigTreeFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(JungleBiome.class)
public abstract class JungleBiomeMixin {

    @Redirect(
            method = "method_3822",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/JungleBiome;field_4631:Lnet/minecraft/world/gen/feature/BigTreeFeature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BigTreeFeature createBigTreeFeature(JungleBiome biome) {
        return ((SQBiome) biome).seedQueue$getBigTreeFeature();
    }
}
