
package com.freemaf.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseParserTest {

    @Test

    void extractsRole() {

        assertEquals("EXECUTOR", ResponseParser.extractRole("[EXECUTOR]\n```\ncode\n```"));

        assertEquals("", ResponseParser.extractRole("no role here"));

    }

    @Test

    void detectsUserChat() {

        assertTrue(ResponseParser.isUserChat("USER_CHAT: hello"));

        assertFalse(ResponseParser.isUserChat("```\ncode\n```"));

    }

    @Test

    void extractsUserChat() {

        // Original extractUserChat strips the raw and returns the tail after USER_CHAT:

        String r = ResponseParser.extractUserChat("USER_CHAT: hello\nworld");

        assertEquals("hello\nworld", r);

    }

    @Test

    void detectsValidScript() {

        assertTrue(ResponseParser.isValidScript("```\nWrite-Output \"ROLE: EXECUTOR\"\n```"));

    }

    @Test

    void rejectsScriptWithRawMarker() {

        assertFalse(ResponseParser.isValidScript("```\nUSER_MSG: hi\n```"));

    }

    @Test

    void rejectsScriptWithSingleFence() {

        assertFalse(ResponseParser.isValidScript("```\nonly open"));

    }

    @Test

    void extractsScript() {

        String s = ResponseParser.extractScript("[EXECUTOR]\n```\nWrite-Output 1\n```");

        assertTrue(s.contains("Write-Output 1"));

    }

    @Test

    void nullInputIsSafe() {

        assertEquals("", ResponseParser.extractRole(null));

        assertFalse(ResponseParser.isUserChat(null));

        assertFalse(ResponseParser.isValidScript(null));

        assertThrows(IllegalArgumentException.class, () -> ResponseParser.extractUserChat(null));

    }

    @Test

    void userChatIgnoresRole() {

        // Original isUserChat checks only raw.strip().startsWith("USER_CHAT:") — role prefix breaks it.

        String r = "[ANALYST]" + System.lineSeparator() + "USER_CHAT: question";

        assertFalse(ResponseParser.isUserChat(r));

    }

    @Test

    void scriptWithRoleInsideDoesNotTripRawMarkerCheck() {

        assertTrue(ResponseParser.isValidScript("[EXECUTOR]\n```\nWrite-Output \"ROLE: EXECUTOR\"\n```"));

    }

    @Test

    void emptyFencedBlockIsAcceptedByOriginal() {

        // Original isValidScript("```\n```") returns true (no content check when exactly two fences present).

        assertTrue(ResponseParser.isValidScript("```\n```"));

    }


    @Test
    void migrationConfirmAcceptedWithoutFence() {
        assertTrue(ResponseParser.isValidMigrationConfirm("AGENT_SESSION_MIGRATE_CONFIRM: ok"));
    }

    @Test
    void migrationConfirmRejectedWhenFencePresent() {
        assertFalse(ResponseParser.isValidMigrationConfirm("```" + System.lineSeparator()
                + "AGENT_SESSION_MIGRATE_CONFIRM: ok" + System.lineSeparator() + "```"));
    }

    @Test
    void migrationConfirmRejectedWhenStartAlsoPresent() {
        String r = "AGENT_SESSION_MIGRATE_START: x" + System.lineSeparator()
                + "AGENT_SESSION_MIGRATE_CONFIRM: y";
        assertFalse(ResponseParser.isValidMigrationConfirm(r));
    }

    @Test
    void migrationConfirmRejectedWhenMultipleConfirms() {
        String r = "AGENT_SESSION_MIGRATE_CONFIRM: a" + System.lineSeparator()
                + "AGENT_SESSION_MIGRATE_CONFIRM: b";
        assertFalse(ResponseParser.isValidMigrationConfirm(r));
    }

    @Test
    void migrationConfirmNullIsSafe() {
        assertFalse(ResponseParser.isValidMigrationConfirm(null));
        assertFalse(ResponseParser.isValidMigrationConfirm(""));
    }

    @Test
    void isValidScriptRejectsRawMigrationStart() {
        assertFalse(ResponseParser.isValidScript("AGENT_SESSION_MIGRATE_START: prep"));
    }

    @Test
    void isValidScriptRejectsRawMigrationConfirm() {
        assertFalse(ResponseParser.isValidScript("AGENT_SESSION_MIGRATE_CONFIRM: done"));
    }
}
