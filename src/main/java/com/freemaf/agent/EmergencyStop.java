package com.freemaf.agent;
public final class EmergencyStop {
    private static volatile boolean stopped = false;
    private EmergencyStop() {}
    public static void reset() { stopped = false; }
    public static void stop() { stopped = true; }
    public static boolean isStopped() { return stopped; }
}
