
package com.freemaf.agent;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Path;

import java.util.ArrayList;

import java.util.List;

public final class ContextPackager {

    private static final Path PROJECT_DIR = Path.of("agent", "project");

    private static final List<String> CONTEXT_FILES = List.of("WIP.md", "MEMORY.md", "PLAN.md");

    public List<Path> listExistingFiles() {

        List<Path> existing = new ArrayList<>();

        for (String fileName : CONTEXT_FILES) {

            Path file = PROJECT_DIR.resolve(fileName);

            if (Files.exists(file)) existing.add(file);

        }

        return existing;

    }

    public String buildContext() {

        StringBuilder sb = new StringBuilder();

        for (Path file : listExistingFiles()) {

            sb.append("=== ").append(file.getFileName()).append(" ===").append(System.lineSeparator());

            sb.append(readOrEmpty(file)).append(System.lineSeparator());

        }

        return sb.toString();

    }

    private String readOrEmpty(Path file) {

        try {

            return Files.readString(file, StandardCharsets.UTF_8);

        } catch (IOException e) {

            AppLogger.warn("ContextPackager read failed: " + e.getMessage());

            return "";

        }

    }

}
