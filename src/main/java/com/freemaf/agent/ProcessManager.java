package com.freemaf.agent;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class ProcessManager {
    private final AtomicReference<Process> currentScriptProcess = new AtomicReference<>();
    private static volatile boolean taskkillDisabledForTest = false;
    public void setCurrentScriptProcess(Process p) {
        if (p == null) {
            currentScriptProcess.set(null);
            return;
        }
        if (!currentScriptProcess.compareAndSet(null, p)) {
            AppLogger.warn("setCurrentScriptProcess: rejected pid=" + p.pid() + " (already set)");
        }
    }
    public void clearCurrentScriptProcess(Process expected) {
        currentScriptProcess.compareAndSet(expected, null);
    }
    public Process getCurrentScriptProcess() { return currentScriptProcess.get(); }
    public void killCurrentProcess() {
        Process p = currentScriptProcess.getAndSet(null);
        if (p == null) return;
        try { p.destroyForcibly(); }
        catch (RuntimeException e) { AppLogger.warn("killCurrentProcess failed: " + e.getMessage()); }
    }
    static void disableTaskkillForTest(boolean v) { taskkillDisabledForTest = v; }
    public void killProcessTreeByPid(long pid) {
        if (taskkillDisabledForTest) return;
        try {
            ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/T", "/PID", String.valueOf(pid));
            Process tk = pb.start();
            tk.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            AppLogger.debug("killProcessTreeByPid taskkill failed pid=" + pid + ": " + e.getMessage());
        }
    }
    public void killProcessTree(Process p) {
        if (p == null || !p.isAlive()) return;
        long pid = p.pid();
        List<Long> descendantsBefore = p.descendants().map(ProcessHandle::pid).collect(Collectors.toList());
        AppLogger.info("killProcessTree: parent=" + pid + ", descendants=" + descendantsBefore);
        boolean taskkillOk = false;
        if (!taskkillDisabledForTest) {
            try {
                ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/T", "/PID", String.valueOf(pid));
                Process tk = pb.start();
                taskkillOk = tk.waitFor(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                AppLogger.debug("killProcessTree taskkill failed parent=" + pid + ": " + e.getMessage());
            }
        }
        if (p.isAlive()) {
            AppLogger.info("killProcessTree: fallback descendants destroyForcibly parent=" + pid);
            List<ProcessHandle> descendants = p.descendants().collect(Collectors.toList());
            for (ProcessHandle h : descendants) {
                try { h.destroyForcibly(); }
                catch (RuntimeException e) { AppLogger.debug("destroyForcibly failed pid=" + h.pid() + ": " + e.getMessage()); }
            }
            try { p.waitFor(3, TimeUnit.SECONDS); }
            catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            for (ProcessHandle h : p.descendants().collect(Collectors.toList())) {
                try { h.destroyForcibly(); }
                catch (RuntimeException e) { AppLogger.debug("destroyForcibly retry failed pid=" + h.pid() + ": " + e.getMessage()); }
            }
            try {
                p.destroyForcibly();
                p.waitFor(3, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                AppLogger.debug("killProcessTree top-level destroyForcibly failed parent=" + pid + ": " + e.getMessage());
            }
        }
        AppLogger.info("killProcessTree: done parent=" + pid + ", taskkillOk=" + taskkillOk + ", alive=" + p.isAlive());
    }
}
