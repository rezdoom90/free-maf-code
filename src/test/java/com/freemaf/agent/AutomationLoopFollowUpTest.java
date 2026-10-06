package com.freemaf.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AutomationLoopFollowUpTest {

    @Test

    void timedOutAddsStopMessage() {

        ExecutionResult r = new ExecutionResult("log", "", -1, true, 1850L);

        String s = AutomationLoop.formatFollowUp(r, "log");

        assertTrue(s.contains("Скрипт остановлен по лимиту 30 минут"));

    }

    @Test

    void warnRangeAddsReminder() {

        ExecutionResult r = new ExecutionResult("log", "", 0, false, 1500L);

        String s = AutomationLoop.formatFollowUp(r, "log");

        assertTrue(s.contains("Напоминание: время выполнения одного скрипта"));

    }

    @Test

    void normalRangeAddsNothing() {

        ExecutionResult r = new ExecutionResult("log", "", 0, false, 300L);

        String s = AutomationLoop.formatFollowUp(r, "log");

        assertFalse(s.contains("Скрипт остановлен"));

        assertFalse(s.contains("Напоминание"));

    }

}
