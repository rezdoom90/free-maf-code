package com.freemaf.agent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class PowerShellExecutorWatchdogTest {
    @TempDir
    Path tmp;
    @Test
    void killsLongRunningScript() throws Exception {
        Config cfg = new Config();
        cfg.set("watchdog.script.timeout.seconds", "2");
        cfg.set("watchdog.script.check.interval.ms", "200");
        PowerShellExecutor ex = new PowerShellExecutor(new ProcessManager(), cfg);
        Path script = tmp.resolve("sleep.ps1");
        Files.writeString(script, "Start-Sleep -Seconds 10", StandardCharsets.UTF_8);
        ExecutionResult r = ex.execute(script);
        assertTrue(r.isTimedOut(), "long script must be marked timed out");
        assertTrue(r.getElapsedSeconds() < 10, "elapsed must be below script duration");
    }
    @Test
    void passesFastScript() throws Exception {
        Config cfg = new Config();
        cfg.set("watchdog.script.timeout.seconds", "10");
        cfg.set("watchdog.script.check.interval.ms", "200");
        PowerShellExecutor ex = new PowerShellExecutor(new ProcessManager(), cfg);
        Path script = tmp.resolve("ok.ps1");
        Files.writeString(script, "Write-Output 'ok'", StandardCharsets.UTF_8);
        ExecutionResult r = ex.execute(script);
        assertFalse(r.isTimedOut(), "fast script must not time out");
        assertTrue(r.getElapsedSeconds() < 5, "elapsed must be small");
        assertTrue(r.stdout().contains("ok"), "stdout must contain ok");
    }
    @Test
    void watchdogSurvivesExceptionInTaskBody() throws Exception {
        Config cfg = new Config();
        cfg.set("watchdog.script.timeout.seconds", "1");
        cfg.set("watchdog.script.check.interval.ms", "200");
        ProcessManager spyPm = spy(new ProcessManager());
        doThrow(new RuntimeException("boom")).doCallRealMethod().when(spyPm).killProcessTree(any(Process.class));
        PowerShellExecutor ex = new PowerShellExecutor(spyPm, cfg);
        Path script = tmp.resolve("survive.ps1");
        Files.writeString(script, "Start-Sleep -Seconds 10", StandardCharsets.UTF_8);
        long startMs = System.currentTimeMillis();
        ExecutionResult r = ex.execute(script);
        long elapsedMs = System.currentTimeMillis() - startMs;
        assertTrue(r.isTimedOut(), "must be marked timed out");
        assertTrue(elapsedMs < 8000L, "must terminate well before script end; elapsed=" + elapsedMs);
    }
}
