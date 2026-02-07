package me.contaria.seedqueue.debug;

import com.google.gson.GsonBuilder;
import com.sun.management.OperatingSystemMXBean;
import me.contaria.seedqueue.SeedQueue;
import org.lwjgl.opengl.GL11;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class SeedQueueSystemInfo {
    public static void logSystemInformation() {
        if (!Boolean.parseBoolean(System.getProperty("seedqueue.logSystemInfo", "true"))) {
            return;
        }
        try {
            List<String> lines = new ArrayList<>();
            lines.add("System Information (Logged by SeedQueue):");
            lines.add(String.format("Operating System: %s", System.getProperty("os.name")));
            lines.add(String.format("OS Version: %s", System.getProperty("os.version")));
            lines.add(String.format("CPU: %s", getCpuInfo()));
            lines.add(String.format("GPU: %s", getGpuInfo()));
            lines.add(String.format("Java Version: %s", System.getProperty("java.version")));
            lines.add(String.format("JVM Arguments: %s", getJavaArguments()));
            lines.add(String.format("Total Physical Memory (MB): %s", getTotalPhysicalMemory()));
            lines.add(String.format("Max Memory (MB): %s", getMaxAllocatedMemory()));
            lines.add(String.format("Total Processors: %s", getTotalPhysicalProcessors()));
            lines.add(String.format("Available Processors: %s", getAvailableProcessors()));
            lines.forEach(SeedQueue.LOGGER::info);
        } catch (Exception e) {
            SeedQueue.LOGGER.error("SeedQueue failed to log System Information!", e);
        }
    }

    private static String getCpuInfo() {
        // see GLX#_init
        oshi.hardware.Processor[] processors = new oshi.SystemInfo().getHardware().getProcessors();
        return String.format("%dx %s", processors.length, processors[0]).replaceAll("\\s+", " ");
    }

    private static String getGpuInfo() {
        // see GlDebugInfo#getRenderer
        return GL11.glGetString(GL11.GL_RENDERER);
    }

    private static String getJavaArguments() {
        // Logs the java arguments being used by the JVM
        return String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    private static long getTotalPhysicalMemory() {
        // Logs the total RAM on the system
        return ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getTotalPhysicalMemorySize() / (1024 * 1024);
    }

    private static long getMaxAllocatedMemory() {
        // Logs the max RAM the JVM will try to use
        return Runtime.getRuntime().maxMemory() / (1024 * 1024);
    }

    private static int getTotalPhysicalProcessors() {
        // Logs the total number of processors on the system
        // also includes the ones which are affected by affinity
        return ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
    }

    private static int getAvailableProcessors() {
        // Logs the available number of processors
        // excludes the ones which are affected by affinity
        return Runtime.getRuntime().availableProcessors();
    }

    public static void logConfigSettings() {
        if (Boolean.parseBoolean(System.getProperty("seedqueue.logConfigSettings", "true"))) {
            SeedQueue.LOGGER.info("SeedQueue Config settings: {}", new GsonBuilder().setPrettyPrinting().create().toJson(SeedQueue.config.container.toJson()));
        }
    }
}
