package me.contaria.seedqueue.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @WrapWithCondition(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Keyboard;enableRepeatEvents(Z)V"
            )
    )
    private boolean doNotEnableRepeatEventsInQueue(Keyboard keyboard, boolean repeatEvents) {
        return !SeedQueue.inQueue();
    }
}
