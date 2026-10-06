
package com.freemaf.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigTest {

    @Test

    void loadsExistingFile() {

        Config config = new Config();

        assertNotNull(config.getWindowTitle());

    }

    @Test

    void defaultIntWhenKeyMissing() {

        Config config = new Config();

        assertEquals(42, config.getInt("nonexistent.test.key", 42));

    }

    @Test

    void defaultLongWhenKeyMissing() {

        Config config = new Config();

        assertEquals(180000L, config.getLong("nonexistent.test.key", 180000L));

    }

    @Test

    void invalidIntFallsBackToDefault() {

        Config config = new Config();

        assertEquals(7, config.getInt("window.title", 7));

    }


    @Test

    void watchdogDefaultsPresent() {

        assertEquals("1800", Config.DEFAULTS.get("watchdog.script.timeout.seconds"));

        assertEquals("1000", Config.DEFAULTS.get("watchdog.script.check.interval.ms"));

        assertEquals("1200", Config.DEFAULTS.get("watchdog.script.warn.threshold.seconds"));

    }
}
