package me.contaria.seedqueue.mixin.included.worldpreview.accessor;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("firstUpdate")
    boolean worldpreview$isFirstUpdate();
}
