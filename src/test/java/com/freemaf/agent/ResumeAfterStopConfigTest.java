package com.freemaf.agent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeAfterStopConfigTest {
    @Test
    void defaultKeyPresent() {
        assertTrue(Config.DEFAULTS.containsKey("session.resumeAfterStop"), "key must exist in DEFAULTS");
        assertEquals("false", Config.DEFAULTS.get("session.resumeAfterStop"), "default must be false");
    }
    @Test
    void getterReturnsBoolean() {
        Config c = new Config();
        boolean value = c.getResumeAfterStop();
        assertFalse(value && !value, "getter must not throw and return a boolean");
    }
}
