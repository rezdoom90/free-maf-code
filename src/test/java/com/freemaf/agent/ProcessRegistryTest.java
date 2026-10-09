package com.freemaf.agent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessRegistryTest {
    private static final Path REGISTRY = Path.of("agent", "cache", "process-registry.txt");
    @BeforeEach
    void clearRegistry() throws IOException {
        Files.deleteIfExists(REGISTRY);
    }
    @AfterEach
    void dropRegistry() throws IOException {
        Files.deleteIfExists(REGISTRY);
    }
    @Test
    void registerWritesLine() throws Exception {
        Process p = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", "Start-Sleep -Seconds 5").start();
        try {
            ProcessRegistry.register(p);
            assertTrue(Files.exists(REGISTRY), "registry file must exist after register");
            String content = Files.readString(REGISTRY, StandardCharsets.UTF_8);
            assertTrue(content.contains("parentPid=" + p.pid() + "|"), "registry must contain parent pid");
        } finally {
            if (p.isAlive()) p.destroyForcibly();
            ProcessRegistry.unregister(p);
        }
    }
    @Test
    void unregisterRemovesLine() throws Exception {
        Process p = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", "Start-Sleep -Seconds 5").start();
        try {
            ProcessRegistry.register(p);
            ProcessRegistry.unregister(p);
            String content = Files.exists(REGISTRY) ? Files.readString(REGISTRY, StandardCharsets.UTF_8) : "";
            assertFalse(content.contains("parentPid=" + p.pid() + "|"), "registry must not contain unregistered pid");
        } finally {
            if (p.isAlive()) p.destroyForcibly();
        }
    }
    @Test
    void cleanupStaleRemovesDeadRecords() throws Exception {
        Process p = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", "Start-Sleep -Seconds 30").start();
        ProcessRegistry.register(p);
        p.destroyForcibly();
        p.waitFor(5, TimeUnit.SECONDS);
        int cleaned = ProcessRegistry.cleanupStale();
        assertTrue(cleaned >= 1, "cleanup must report at least one stale record, got " + cleaned);
        String content = Files.exists(REGISTRY) ? Files.readString(REGISTRY, StandardCharsets.UTF_8) : "";
        assertFalse(content.contains("parentPid=" + p.pid() + "|"), "dead record must be removed");
    }
}
