package me.contaria.seedqueue.synchronization;

public class ThreadedFallingBlock {
    public static final ThreadLocal<Boolean> instantFall = ThreadLocal.withInitial(() -> false);
}
