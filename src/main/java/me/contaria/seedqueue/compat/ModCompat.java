package me.contaria.seedqueue.compat;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Intermediate class that allows safe access to other mods methods/fields from code paths that may be run without the mod present.
 * This class provides wrapper methods for compat classes which should only be classloaded if the mod in question is loaded.
 */
public class ModCompat {
    public static final boolean HAS_STANDARDSETTINGS = FabricLoader.getInstance().isModLoaded("standardsettings");
    public static final boolean HAS_STATEOUTPUT = FabricLoader.getInstance().isModLoaded("state-output");

    public static void stateoutput$setWallState() {
        if (HAS_STATEOUTPUT) {
            StateOutputCompat.setWallState();
        }
    }

    public static void standardsettings$cache() {
        if (HAS_STANDARDSETTINGS) {
            StandardSettingsCompat.createCache();
        }
    }

    public static void standardsettings$reset() {
        if (HAS_STANDARDSETTINGS) {
            StandardSettingsCompat.resetPendingActions();
            if (StandardSettingsCompat.isEnabled()) {
                StandardSettingsCompat.reset();
            }
        }
    }

    public static void standardsettings$loadCache() {
        if (HAS_STANDARDSETTINGS) {
            StandardSettingsCompat.loadCache();
        }
    }
}
