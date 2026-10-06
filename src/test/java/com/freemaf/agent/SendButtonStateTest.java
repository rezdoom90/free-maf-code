
package com.freemaf.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SendButtonStateTest {

    @Test

    void disabledWhenNoTextAndNoFiles() {

        assertFalse(SendButtonState.isEnabled(null, 0));

        assertFalse(SendButtonState.isEnabled("", 0));

        assertFalse(SendButtonState.isEnabled("   ", 0));

    }

    @Test

    void enabledWhenTextPresent() {

        assertTrue(SendButtonState.isEnabled("hi", 0));

        assertTrue(SendButtonState.isEnabled("  hi  ", 0));

    }

    @Test

    void enabledWhenFilesPresent() {

        assertTrue(SendButtonState.isEnabled("", 1));

        assertTrue(SendButtonState.isEnabled(null, 3));

        assertTrue(SendButtonState.isEnabled("text", 2));

    }

}
