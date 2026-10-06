package com.freemaf.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DirectiveValidatorTest {

    @Test
    void allowedWriteToOrdinarySourceFile() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("Set-Content -Path src/Main.java -Value 'x'");
        assertTrue(r.allowed());
    }

    @Test
    void allowedWriteToMemoryMd() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("Set-Content -Path agent/project/MEMORY.md -Value 'x'");
        assertTrue(r.allowed());
    }

    @Test
    void allowedAddContentWithDotSlashPrefix() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("Add-Content ./agent/project/MEMORY.md 'x'");
        assertTrue(r.allowed());
    }

    @Test
    void allowedReadOfProtectedFile() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("Get-Content -Raw agent/rules/EXECUTOR.md");
        assertTrue(r.allowed());
    }

    @Test
    void allowedMentionOfProtectedPathInComment() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("# see agent/rules/EXECUTOR.md for details");
        assertTrue(r.allowed());
    }

    @Test
    void blockedSetContentExecutorRuntime() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("Set-Content -Path agent/rules/EXECUTOR.md -Value 'x'");
        assertFalse(r.allowed());
        assertTrue(r.reason().contains("rules/executor.md"));
    }

    @Test
    void blockedAddContentJudge() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("Add-Content agent/rules/JUDGE.md 'x'");
        assertFalse(r.allowed());
        assertTrue(r.reason().contains("rules/judge.md"));
    }

    @Test
    void blockedOutFileInstructions() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("Out-File agent/AGENT_INSTRUCTIONS.md");
        assertFalse(r.allowed());
        assertTrue(r.reason().contains("agent_instructions.md"));
    }

    @Test
    void blockedBackslashPath() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("Set-Content .\\agent\\rules\\CODE_REVIEWER.md 'x'");
        assertFalse(r.allowed());
        assertTrue(r.reason().contains("rules/code_reviewer.md"));
    }

    @Test
    void blockedLowercasePath() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("Set-Content agent/rules/code_reviewer.md 'x'");
        assertFalse(r.allowed());
        assertTrue(r.reason().contains("rules/code_reviewer.md"));
    }

    @Test
    void blockedRedirection() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("'x' > agent/rules/EXECUTOR.md");
        assertFalse(r.allowed());
        assertTrue(r.reason().contains("rules/executor.md"));
    }

    @Test
    void blockedSourceLayoutWithoutPrefix() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("Set-Content rules/EXECUTOR.md -Value 'x'");
        assertFalse(r.allowed());
        assertTrue(r.reason().contains("rules/executor.md"));
    }

    @Test
    void blockedSourceLayoutInstructions() {
        DirectiveValidator.ValidationResult r = DirectiveValidator.validate("Set-Content AGENT_INSTRUCTIONS.md -Value 'x'");
        assertFalse(r.allowed());
        assertTrue(r.reason().contains("agent_instructions.md"));
    }
}
