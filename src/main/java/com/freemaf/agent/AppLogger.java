
package com.freemaf.agent;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.StandardOpenOption;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

public final class AppLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Path LOG_FILE = Path.of("agent", "app.log");

    private AppLogger() {}

    public static boolean isDebugEnabled() { return true; }

    public static synchronized void info(String msg) {

        write("INFO", msg);

    }

    public static synchronized void debug(String msg) {

        if (isDebugEnabled()) write("DEBUG", msg);

    }

    public static synchronized void warn(String msg) {

        write("WARN", msg);

    }

    public static synchronized void error(String msg) {

        write("ERROR", msg);

    }

    private static void write(String level, String msg) {

        String line = LocalDateTime.now().format(FORMATTER) + " [" + level + "] " + msg;

        System.out.println(line);

        try {

            Files.createDirectories(LOG_FILE.getParent());

            Files.writeString(LOG_FILE, line + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {

            System.err.println("AppLogger error: " + e.getMessage());

        }

    }

}
