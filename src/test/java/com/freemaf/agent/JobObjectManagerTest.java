package com.freemaf.agent;
import com.freemaf.agent.winapi.JobObjectManager;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JobObjectManagerTest {
    @Test
    void killOnCloseKillsAssignedProcess() throws Exception {
        assumeTrue(JobObjectManager.isActive(), "job object not active");
        Process p = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", "Start-Sleep -Seconds 30").start();
        try {
            boolean ok = JobObjectManager.assign(p);
            assertTrue(ok, "assign must succeed");
            JobObjectManager.closeHandleForTest();
            boolean dead = p.waitFor(2, TimeUnit.SECONDS);
            assertTrue(dead, "assigned process must be killed after job handle close");
        } finally {
            if (p.isAlive()) { p.destroyForcibly(); }
            JobObjectManager.recreateForTest();
        }
    }
    @Test
    void unassignedProcessSurvivesHandleClose() throws Exception {
        assumeTrue(JobObjectManager.isActive(), "job object not active");
        Process p = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", "Start-Sleep -Seconds 5").start();
        try {
            JobObjectManager.closeHandleForTest();
            Thread.sleep(1500L);
            assertTrue(p.isAlive(), "unassigned process must survive handle close");
        } finally {
            if (p.isAlive()) { p.destroyForcibly(); }
            JobObjectManager.recreateForTest();
        }
    }
}
