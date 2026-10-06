
package com.freemaf.agent;

import java.io.IOException;

import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Path;

import java.util.concurrent.CompletableFuture;

import java.util.concurrent.Executors;

import java.util.concurrent.ScheduledExecutorService;

import java.util.concurrent.TimeUnit;

import java.util.function.Consumer;

public final class PowerShellExecutor {

    private final ProcessManager processManager;

    private final Config config;

    private volatile long processStartMs;

    private ScheduledExecutorService watchdog;

    private volatile boolean timedOutFlag;

    public PowerShellExecutor(ProcessManager processManager, Config config) {

        this.processManager = processManager;

        this.config = config;

    }

    public ExecutionResult execute(Path script, Consumer<String> lineConsumer) throws IOException, InterruptedException {

        ExecutionResult result = execute(script);

        if (lineConsumer != null) {

            for (String line : result.stdout().split("\r?\n")) {

                if (!line.isEmpty()) lineConsumer.accept(line);

            }

            for (String line : result.stderr().split("\r?\n")) {

                if (!line.isEmpty()) lineConsumer.accept("[stderr] " + line);

            }

        }

        return result;

    }

    public ExecutionResult execute(Path script) throws IOException, InterruptedException {

        String scriptContent = Files.readString(script, StandardCharsets.UTF_8);

        ScriptLogger.save(scriptContent);

        String scriptPath = script.toAbsolutePath().toString().replace("'", "''");

        String command = "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; "

                + "$OutputEncoding = [System.Text.Encoding]::UTF8; "

                + "& '" + scriptPath + "'";

        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", command);

        pb.directory(Path.of(".").toAbsolutePath().normalize().toFile());

        pb.redirectErrorStream(false);

        Process process = pb.start();

        processStartMs = System.currentTimeMillis();

        timedOutFlag = false;

        processManager.setCurrentScriptProcess(process);

        long checkMs = config.getScriptCheckIntervalMs();

        long timeoutSec = config.getScriptTimeoutSeconds();

        ScheduledExecutorService wd = Executors.newSingleThreadScheduledExecutor(r -> {

            Thread t = new Thread(r, "ps-watchdog");

            t.setDaemon(true);

            return t;

        });

        watchdog = wd;

        wd.scheduleWithFixedDelay(() -> {

            long elapsed = System.currentTimeMillis() - processStartMs;

            if (elapsed > timeoutSec * 1000L) {

                timedOutFlag = true;

                processManager.killProcessTree(process);

                wd.shutdown();

            }

        }, checkMs, checkMs, TimeUnit.MILLISECONDS);

        CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(() -> readStream(process.getInputStream()));

        CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() -> readStream(process.getErrorStream()));

        try {

            process.waitFor();

        } finally {

            watchdog.shutdownNow();

            watchdog = null;

        }

        long elapsedSeconds = (System.currentTimeMillis() - processStartMs) / 1000L;

        String stdout = stdoutFuture.join();

        String stderr = stderrFuture.join();

        int exitCode = process.exitValue();

        ExecutionResult result = new ExecutionResult(stdout, stderr, exitCode, timedOutFlag, elapsedSeconds);

        ExecutionLogger.save(result);

        processManager.setCurrentScriptProcess(null);

        return result;

    }

    private static String readStream(InputStream in) {

        try (in) {

            return new String(in.readAllBytes(), StandardCharsets.UTF_8);

        } catch (IOException e) {

            AppLogger.warn("PowerShell stream read failed: " + e.getMessage());

            return "";

        }

    }

}
