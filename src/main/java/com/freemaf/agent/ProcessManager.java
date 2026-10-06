
package com.freemaf.agent;

import java.util.concurrent.TimeUnit;

public final class ProcessManager {

    private volatile Process currentScriptProcess;

    public void setCurrentScriptProcess(Process p) { this.currentScriptProcess = p; }

    public void killCurrentProcess() {

        Process p = currentScriptProcess;

        if (p == null) return;

        try { p.destroyForcibly(); }

        catch (RuntimeException e) { AppLogger.warn("killCurrentProcess failed: " + e.getMessage()); }

        currentScriptProcess = null;

    }


    public void killProcessTree(Process p) {

        if (p == null || !p.isAlive()) return;

        try {

            ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/T", "/PID", String.valueOf(p.pid()));

            Process tk = pb.start();

            tk.waitFor(5, TimeUnit.SECONDS);

        } catch (Exception e) {

            AppLogger.debug("killProcessTree taskkill failed: " + e.getMessage());

        }

        if (p.isAlive()) {

            try {

                p.destroyForcibly();

                p.waitFor(3, TimeUnit.SECONDS);

            } catch (Exception e) {

                AppLogger.debug("killProcessTree destroyForcibly failed: " + e.getMessage());

            }

        }

    }
}
