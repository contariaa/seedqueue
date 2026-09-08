package me.contaria.seedqueue.mixin.server.synchronization;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.util.RandomVectorGenerator;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RandomVectorGenerator.class)
public abstract class RandomVectorGeneratorMixin {

    @Redirect(
            method = {
                    "method_2800",
                    "method_2801"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/RandomVectorGenerator;field_3660:Lnet/minecraft/util/math/Vec3d;",
                    opcode = Opcodes.PUTSTATIC
            )
    )
    private static void setTemporaryVec3d(Vec3d value, @Share("temp") LocalRef<Vec3d> temp) {
        temp.set(value);
    }

    @Redirect(
            method = {
                    "method_2800",
                    "method_2801"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/RandomVectorGenerator;field_3660:Lnet/minecraft/util/math/Vec3d;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static Vec3d setTemporaryVec3d(@Share("temp") LocalRef<Vec3d> temp) {
        return temp.get();
    }
}
