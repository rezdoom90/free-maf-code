
package com.freemaf.agent;

public enum SpeedMode {

    INSTANT("Instant", 0L, 0L),

    FAST("Fast", 30_000L, 150_000L),

    NORMAL("Normal", 190_000L, 320_000L),

    SLOW("Slow", 350_000L, 600_000L),

    VERY_SLOW("Very Slow", 600_000L, 1_100_000L);

    private final String label;

    private final long minMs;

    private final long maxMs;

    SpeedMode(String label, long minMs, long maxMs) {

        this.label = label;

        this.minMs = minMs;

        this.maxMs = maxMs;

    }

    public String getLabel() { return label; }

    public long getMinMs() { return minMs; }

    public long getMaxMs() { return maxMs; }

    public static SpeedMode fromName(String name) {

        if (name == null) return INSTANT;

        try { return SpeedMode.valueOf(name.trim().toUpperCase()); }

        catch (IllegalArgumentException e) { return INSTANT; }

    }

    public SpeedMode next() {

        SpeedMode[] all = values();

        return all[(ordinal() + 1) % all.length];

    }

    @Override public String toString() { return label; }

}
