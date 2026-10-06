package com.freemaf.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerShellExecutorDirectiveValidatorTest {

    @TempDir
    Path tmp;

    @Test
    void blocksWriteToProtectedDirectiveFile() throws Exception {
        Config cfg = new Config();
        PowerShellExecutor ex = new PowerShellExecutor(new ProcessManager(), cfg);

        Path protectedFile = Path.of("rules/EXECUTOR.md").toAbsolutePath().normalize();
        assertTrue(Files.exists(protectedFile), "fixture assumption: rules/EXECUTOR.md must exist");
        String contentBefore = Files.readString(protectedFile, StandardCharsets.UTF_8);

        Path script = tmp.resolve("blocked.ps1");
        Files.writeString(script,
                "Set-Content -Path rules/EXECUTOR.md -Value 'x'",
                StandardCharsets.UTF_8);

        ExecutionResult r = ex.execute(script);

        assertEquals(-1, r.exitCode(), "blocked script must not execute");
        assertTrue(r.stderr().contains("protected directive file"),
                "stderr must describe the protection: " + r.stderr());
        assertEquals(contentBefore, Files.readString(protectedFile, StandardCharsets.UTF_8),
                "protected file must not change");
    }
}
