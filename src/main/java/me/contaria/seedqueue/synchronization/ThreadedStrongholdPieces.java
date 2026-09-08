package me.contaria.seedqueue.synchronization;

import java.util.List;

public class ThreadedStrongholdPieces {
    public static final ThreadLocal<List<?>> THREADED_POSSIBLE_PIECES = new ThreadLocal<>();
    public static final ThreadLocal<Class<?>> THREADED_ACTIVE_PIECE_TYPE = new ThreadLocal<>();
    public static final ThreadLocal<Integer> THREADED_TOTAL_WEIGHT = ThreadLocal.withInitial(() -> 0);
}
