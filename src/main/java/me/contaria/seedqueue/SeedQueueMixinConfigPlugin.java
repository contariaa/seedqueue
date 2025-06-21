package me.contaria.seedqueue;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import me.contaria.speedrunapi.mixin_plugin.SpeedrunMixinConfigPlugin;

import java.util.List;

public class SeedQueueMixinConfigPlugin extends SpeedrunMixinConfigPlugin implements MixinCanceller {

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        // SleepBackgrounds Thread Executor mixin has been observed to hurt performance when SeedQueue is active
        // since SeedQueue spawns many more executors
        return mixinClassName.equals("com.redlimerl.sleepbackground.mixin.MixinThreadExecutor");
    }
}
