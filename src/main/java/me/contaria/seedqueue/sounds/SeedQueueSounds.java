package me.contaria.seedqueue.sounds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.util.Identifier;

public class SeedQueueSounds {
    public static final Identifier PLAY_INSTANCE = register("play_instance");
    public static final Identifier LOCK_INSTANCE = register("lock_instance");
    public static final Identifier RESET_INSTANCE = register("reset_instance");
    public static final Identifier RESET_ALL = register("reset_all");
    public static final Identifier RESET_COLUMN = register("reset_column");
    public static final Identifier RESET_ROW = register("reset_row");
    public static final Identifier SCHEDULE_JOIN = register("schedule_join");
    public static final Identifier SCHEDULE_ALL = register("schedule_all");
    public static final Identifier SCHEDULED_JOIN_WARNING = register("scheduled_join_warning");
    public static final Identifier START_BENCHMARK = register("start_benchmark");
    public static final Identifier FINISH_BENCHMARK = register("finish_benchmark");
    public static final Identifier OPEN_WALL = register("open_wall");
    public static final Identifier BYPASS_WALL = register("bypass_wall");

    public static void init() {
    }

    private static Identifier register(String id) {
        return new Identifier("seedqueue", id);
    }

    public static boolean play(Identifier sound) {
        SoundManager manager = MinecraftClient.getInstance().getSoundManager();
        WeightedSoundSet soundSet = manager.get(sound);
        if (soundSet == null || soundSet.getSound() == SoundManager.MISSING_SOUND) {
            return false;
        }
        manager.play(PositionedSoundInstance.master(sound, 1.0f));
        return true;
    }

    public static boolean play(Identifier sound, int delay) {
        SoundManager manager = MinecraftClient.getInstance().getSoundManager();
        WeightedSoundSet soundSet = manager.get(sound);
        if (soundSet == null || soundSet.getSound() == SoundManager.MISSING_SOUND) {
            return false;
        }
        manager.play(PositionedSoundInstance.master(sound, 1.0f), delay);
        return true;
    }
}
