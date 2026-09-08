package me.contaria.seedqueue.mixin.included.worldpreview.accessor;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Invoker("tickLightmap")
    void worldpreview$tickLightmap();

    @Invoker("setupCamera")
    void worldpreview$setupCamera(float tickDelta, int anaglyphFilter);
}
