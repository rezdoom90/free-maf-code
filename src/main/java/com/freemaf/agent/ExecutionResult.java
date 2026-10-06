package com.freemaf.agent;

public record ExecutionResult(String stdout, String stderr, int exitCode, boolean timedOut, long elapsedSeconds) {

    public ExecutionResult(String stdout, String stderr, int exitCode) {

        this(stdout, stderr, exitCode, false, 0L);

    }

    public boolean isTimedOut() { return timedOut; }

    public long getElapsedSeconds() { return elapsedSeconds; }

}
