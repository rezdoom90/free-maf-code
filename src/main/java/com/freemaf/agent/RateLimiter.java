
package com.freemaf.agent;

public final class RateLimiter {

    private static long lastAction;

    private RateLimiter() {}

    public static synchronized void pause() {

        long now = System.currentTimeMillis();

        long wait = AgentConstants.MIN_INTERVAL_MS - (now - lastAction);

        if (wait > 0) {

            try {

                Thread.sleep(wait);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

            }

        }

        lastAction = System.currentTimeMillis();

    }

}
