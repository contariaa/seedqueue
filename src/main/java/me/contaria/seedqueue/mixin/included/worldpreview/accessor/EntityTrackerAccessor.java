package me.contaria.seedqueue.mixin.included.worldpreview.accessor;

import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.TrackedEntityInstance;
import net.minecraft.util.collection.IntObjectStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityTracker.class)
public interface EntityTrackerAccessor {
    @Accessor("trackedEntityIds")
    IntObjectStorage<TrackedEntityInstance> worldpreview$getTrackedEntityIds();
}
