package me.contaria.seedqueue.mixin.server.synchronization;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.TypeFilterableList;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.Set;

@Mixin(TypeFilterableList.class)
public abstract class TypeFilterableListMixin {

    @WrapOperation(
            method = "<clinit>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/TypeFilterableList;field_11829:Ljava/util/Set;",
                    opcode = Opcodes.PUTSTATIC
            )
    )
    private static void synchronizeTypeSet(Set<Class<?>> set, Operation<Void> original) {
        original.call(Collections.synchronizedSet(set));
    }
}
