package me.contaria.seedqueue.compat;

import me.duncanruns.hermes.api.HermesModAPI;
import net.minecraft.server.MinecraftServer;

public class HermesCompat {
    public static void writeToWorldLog(MinecraftServer server, String type, long time) {
        HermesModAPI.writeToWorldLog(HermesModAPI.getSavePath(server), type, time);
    }
}
