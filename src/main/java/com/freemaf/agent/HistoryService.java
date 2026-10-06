
package com.freemaf.agent;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.StandardOpenOption;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

public final class HistoryService {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private HistoryService() {

    }

    public static void savePrompt(String prompt) {

        write("prompts", prompt);

    }

    public static void saveResponse(String response) {

        write("responses", response);

    }

    static void write(String subfolder, String content) {

        Path dir = Path.of("agent", "history", subfolder);

        try {

            Files.createDirectories(dir);

            String fileName = LocalDateTime.now().format(TIMESTAMP) + "_" + System.nanoTime() + ".txt";

            Path file = dir.resolve(fileName);

            Files.writeString(file, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);

        } catch (IOException e) {

            AppLogger.warn("HistoryService write failed: " + e.getMessage());

        }

    }

}
