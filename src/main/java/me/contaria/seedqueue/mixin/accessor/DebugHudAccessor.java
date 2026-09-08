package me.contaria.seedqueue.mixin.accessor;

import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DebugHud.class)
public interface DebugHudAccessor {
    @Invoker("drawMetricsData")
    void seedQueue$drawMetricsData();
}
